package dev.bacteriawa.mint.scheduler;

import ca.spottedleaf.concurrentutil.scheduler.SchedulableTick;
import ca.spottedleaf.concurrentutil.scheduler.Scheduler;
import ca.spottedleaf.concurrentutil.scheduler.SchedulerAccess;
import ca.spottedleaf.concurrentutil.util.TimeUtil;
import io.papermc.paper.threadedregions.TickRegionScheduler;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

public final class HMBIRDSchedulerThreadPool extends Scheduler {

    private final ThreadFactory threadFactory;
    private final int threadCount;
    private final Thread[] threads;
    private final Worker[] workers;

    private final AtomicBoolean halted = new AtomicBoolean();

    // Ordered by scheduled start time, then by tick id.
    private final PriorityQueue<TickState> scheduledTicks = new PriorityQueue<>((a, b) -> {
        final int cmp = TimeUtil.compareTimes(SchedulerAccess.getScheduledStart(a.tick), SchedulerAccess.getScheduledStart(b.tick));
        if (cmp != 0) {
            return cmp;
        }
        return Long.signum(a.tick.id - b.tick.id);
    });
    // Guards scheduledTicks and timer thread coordination.
    private final Object scheduleLock = new Object();
    private volatile Thread timerThread;

    // Global queue for ready tasks, local queues for affinity/steal.
    private final HMBIRDDispatchQueue globalQueue = new HMBIRDDispatchQueue();
    private final HMBIRDLocalQueue[] localQueues;
    private final HMBIRDMetrics metrics = new HMBIRDMetrics();

    // Count of intermediate tasks currently enqueued.
    private final AtomicLong pendingIntermediate = new AtomicLong();

    // Time budget for intermediate tasks per tick.
    private volatile long intermediateTimeSliceNs = TimeUnit.MILLISECONDS.toNanos(2L);
    // Backpressure thresholds for intermediate tasks.
    private volatile long rejectGlobalQueueSize = 200_000L;
    private volatile long recoverGlobalQueueSize = 100_000L;
    private volatile boolean droppingIntermediateTasks;

    public HMBIRDSchedulerThreadPool(final int threadCount, final ThreadFactory threadFactory) {
        this.threadFactory = Objects.requireNonNull(threadFactory, "threadFactory");
        this.threadCount = Math.max(1, threadCount);

        this.localQueues = new HMBIRDLocalQueue[this.threadCount];
        for (int i = 0; i < this.localQueues.length; ++i) {
            this.localQueues[i] = new HMBIRDLocalQueue();
        }

        this.workers = new Worker[this.threadCount];
        this.threads = new Thread[this.threadCount + 1];

        for (int i = 0; i < this.threadCount; ++i) {
            final Worker worker = new Worker(i);
            this.workers[i] = worker;
            this.threads[i] = this.threadFactory.newThread(worker);
        }

        this.threads[this.threadCount] = new Thread(this::timerLoop, "HMBIRD Scheduler Timer");
        this.timerThread = this.threads[this.threadCount];
        this.timerThread.setDaemon(true);
    }

    public void start() {
        for (final Thread thread : this.threads) {
            thread.start();
        }
    }

    public void setIntermediateTimeSliceNs(final long sliceNs) {
        this.intermediateTimeSliceNs = Math.max(0L, sliceNs);
    }

    public void setQueueRejectThresholds(final long rejectSize, final long recoverSize) {
        this.rejectGlobalQueueSize = Math.max(0L, rejectSize);
        this.recoverGlobalQueueSize = Math.max(0L, recoverSize);
    }

    public HMBIRDMetrics.Snapshot getMetricsSnapshot() {
        return this.metrics.snapshot(this.pendingIntermediate.get(), this.droppingIntermediateTasks);
    }

    @Override
    public Thread[] getAliveThreads() {
        int count = 0;
        for (final Thread thread : this.threads) {
            if (thread.isAlive()) {
                ++count;
            }
        }
        final Thread[] ret = new Thread[count];
        int idx = 0;
        for (final Thread thread : this.threads) {
            if (thread.isAlive()) {
                ret[idx++] = thread;
            }
        }
        return ret;
    }

    @Override
    public Thread[] getCoreThreads() {
        final Thread[] ret = new Thread[this.threadCount];
        System.arraycopy(this.threads, 0, ret, 0, this.threadCount);
        return ret;
    }

    @Override
    public void halt() {
        if (!this.halted.compareAndSet(false, true)) {
            return;
        }
        for (final Thread thread : this.threads) {
            LockSupport.unpark(thread);
        }
        synchronized (this.scheduleLock) {
            this.scheduleLock.notifyAll();
        }
    }

    @Override
    public boolean join(final long msToWait) {
        try {
            return this.joinInterruptable(msToWait);
        } catch (final InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public boolean joinInterruptable(final long msToWait) throws InterruptedException {
        final long deadlineNs = msToWait <= 0L ? 0L : System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(msToWait);
        for (final Thread thread : this.threads) {
            while (thread.isAlive()) {
                if (msToWait <= 0L) {
                    thread.join();
                    continue;
                }
                final long now = System.nanoTime();
                if (now - deadlineNs >= 0L) {
                    return false;
                }
                thread.join(TimeUnit.NANOSECONDS.toMillis(deadlineNs - now));
            }
        }
        return true;
    }

    @Override
    public void schedule(final SchedulableTick tick) {
        if (SchedulerAccess.getScheduledStart(tick) == TimeUtil.DEADLINE_NOT_SET) {
            throw new IllegalStateException("Task start is not set");
        }

        final TickState state = new TickState(tick);
        if (!SchedulerAccess.setState(tick, state)) {
            throw new IllegalStateException("Task " + tick + " is already scheduled or cancelled");
        }

        synchronized (this.scheduleLock) {
            if (this.halted.get()) {
                return;
            }
            if (state.tryMarkScheduled()) {
                this.metrics.onSchedule();
                this.scheduledTicks.add(state);
                this.scheduleLock.notifyAll();
            }
        }
    }

    @Override
    public void notifyTasks(final SchedulableTick tick) {
        final TickState state = (TickState)SchedulerAccess.getState(tick);
        if (state == null || !state.isScheduled()) {
            return;
        }
        this.metrics.onNotifyTasks();

        // Apply simple backpressure when the global queue is too large.
        if (this.droppingIntermediateTasks && this.pendingIntermediate.get() < this.recoverGlobalQueueSize) {
            this.droppingIntermediateTasks = false;
        }
        if (this.droppingIntermediateTasks) {
            this.metrics.onRejected();
            return;
        }

        if (!state.tasksNotified.compareAndSet(false, true)) {
            return;
        }

        final long pending = this.pendingIntermediate.incrementAndGet();
        if (pending > this.rejectGlobalQueueSize) {
            this.droppingIntermediateTasks = true;
            this.pendingIntermediate.decrementAndGet();
            state.tasksNotified.set(false);
            this.metrics.onRejected();
            return;
        }

        final long now = System.nanoTime();
        final HMBIRDSchedProp prop = new HMBIRDSchedProp(HMBIRDSchedProp.DEADLINE_LEVEL2, this.affinityDomain(tick), false, true);
        final HMBIRDTask task = new HMBIRDTask(() -> {
            this.pendingIntermediate.decrementAndGet();
            state.tasksNotified.set(false);
            this.metrics.onIntermediateEnqueued(Math.max(0L, System.nanoTime() - now));
            this.runIntermediateTasks(state);
            this.metrics.onIntermediateExecuted();
        }, prop, now, now);
        this.offer(task);
        this.wakeWorkers();
    }

    @Override
    public boolean cancel(final SchedulableTick tick) {
        final TickState state = (TickState)SchedulerAccess.getState(tick);
        if (state == null) {
            return false;
        }
        if (!state.tryMarkCancelled()) {
            return false;
        }
        this.metrics.onCancelled();
        this.wakeWorkers();
        synchronized (this.scheduleLock) {
            this.scheduleLock.notifyAll();
        }
        return true;
    }

    private void wakeWorkers() {
        for (int i = 0; i < this.threadCount; ++i) {
            LockSupport.unpark(this.threads[i]);
        }
    }

    private int affinityDomain(final SchedulableTick tick) {
        if (tick instanceof TickRegionScheduler.RegionScheduleHandle handle && handle.region != null) {
            return Long.hashCode(handle.region.id);
        }
        return Long.hashCode(tick.id);
    }

    private void offer(final HMBIRDTask task) {
        // Future tasks wait in the global queue; high-deadline tasks keep affinity.
        final long now = System.nanoTime();
        if (task.scheduledNanos() > now) {
            this.globalQueue.offer(task);
            return;
        }

        if (task.prop().deadlineLevel() >= HMBIRDSchedProp.DEADLINE_LEVEL2) {
            final int workerId = Math.floorMod(task.prop().affinityDomain(), this.threadCount);
            this.localQueues[workerId].offer(task);
            return;
        }

        this.globalQueue.offer(task);
    }

    private void timerLoop() {
        while (!this.halted.get()) {
            TickState next = null;
            synchronized (this.scheduleLock) {
                while (!this.halted.get()) {
                    final TickState peek = this.scheduledTicks.peek();
                    next = peek;
                    if (next == null) {
                        try {
                            this.scheduleLock.wait(1_000L);
                        } catch (final InterruptedException ignored) {
                        }
                        continue;
                    }
                    if (!next.isScheduled()) {
                        this.scheduledTicks.poll();
                        continue;
                    }
                    final long scheduledStart = SchedulerAccess.getScheduledStart(next.tick);
                    final long now = System.nanoTime();
                    if (TimeUtil.compareTimes(scheduledStart, now) <= 0) {
                        next = this.scheduledTicks.poll();
                        break;
                    }
                    final long waitNs = scheduledStart - now;
                    try {
                        this.scheduleLock.wait(Math.max(1L, TimeUnit.NANOSECONDS.toMillis(waitNs)));
                    } catch (final InterruptedException ignored) {
                    }
                }
                if (this.halted.get()) {
                    return;
                }
            }

            if (next == null || !next.isScheduled()) {
                continue;
            }

            // Timer thread converts due ticks into runnable tasks.
            final long enqueue = System.nanoTime();
            final long scheduledStart = SchedulerAccess.getScheduledStart(next.tick);
            final TickState due = next;
            final HMBIRDSchedProp prop = HMBIRDSchedProp.deadlineLevel3(this.affinityDomain(due.tick));
            final HMBIRDTask execTick = new HMBIRDTask(() -> this.runTick(due), prop, enqueue, scheduledStart);
            this.offer(execTick);
            this.wakeWorkers();
        }
    }

    private void runTick(final TickState state) {
        if (!state.tryEnterTick()) {
            if (state.isScheduled() && !state.isCancelled()) {
                this.rescheduleTimer(state);
            }
            return;
        }
        try {
            if (state.isCancelled()) {
                return;
            }
            final long scheduledStart = SchedulerAccess.getScheduledStart(state.tick);
            if (scheduledStart != TimeUtil.DEADLINE_NOT_SET) {
                this.metrics.onTickEnqueued(Math.max(0L, System.nanoTime() - scheduledStart));
            }

            final boolean reschedule = state.tick.runTick();
            this.metrics.onTickExecuted();
            if (!reschedule) {
                state.tryMarkCancelled();
                return;
            }

            if (state.isCancelled()) {
                return;
            }

            if (SchedulerAccess.getScheduledStart(state.tick) == TimeUtil.DEADLINE_NOT_SET) {
                state.tryMarkCancelled();
                return;
            }

            this.rescheduleTimer(state);
        } finally {
            state.exitExecution();
        }
    }

    private void runIntermediateTasks(final TickState state) {
        if (!state.tryEnterTasks()) {
            return;
        }
        try {
            if (state.isCancelled()) {
                return;
            }
            if (!state.tick.hasTasks()) {
                return;
            }

            // Run until time slice or next scheduled tick deadline.
            final long start = System.nanoTime();
            final long sliceEnd = this.intermediateTimeSliceNs <= 0L ? start : start + this.intermediateTimeSliceNs;
            final long tickStart = SchedulerAccess.getScheduledStart(state.tick);
            final long deadline = tickStart == TimeUtil.DEADLINE_NOT_SET ? sliceEnd : Math.min(sliceEnd, tickStart);

            final BooleanSupplier canContinue = () -> {
                final long now = System.nanoTime();
                return now - deadline < 0L && !state.isCancelled();
            };
            final boolean keep = state.tick.runTasks(canContinue);
            if (!keep) {
                state.tryMarkCancelled();
            }
        } finally {
            state.exitExecution();
        }
    }

    private void rescheduleTimer(final TickState state) {
        synchronized (this.scheduleLock) {
            if (this.halted.get() || !state.isScheduled()) {
                return;
            }
            this.scheduledTicks.add(state);
            this.scheduleLock.notifyAll();
        }
    }

    private HMBIRDTask pollWork(final int workerId) {
        final long now = System.nanoTime();

        // Local -> ready global -> steal -> global any.
        final HMBIRDTask local = this.localQueues[workerId].pollAny();
        if (local != null) {
            return local;
        }

        final HMBIRDTask ready = this.globalQueue.pollReady(now);
        if (ready != null) {
            return ready;
        }

        for (int i = 1; i < this.threadCount; ++i) {
            final int victim = (workerId + i) % this.threadCount;
            final HMBIRDTask stolen = this.localQueues[victim].stealAny();
            if (stolen != null) {
                return stolen;
            }
        }

        return this.globalQueue.pollAny();
    }

    private final class Worker implements Runnable {

        private final int id;

        private Worker(final int id) {
            this.id = id;
        }

        @Override
        public void run() {
            while (!HMBIRDSchedulerThreadPool.this.halted.get()) {
                final HMBIRDTask task = HMBIRDSchedulerThreadPool.this.pollWork(this.id);
                if (task == null) {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1L));
                    continue;
                }
                try {
                    task.run();
                } catch (final Throwable ignored) {
                }
            }
        }
    }

    private static final class TickState {

        private static final int STATE_NOT_SCHEDULED = 0;
        private static final int STATE_SCHEDULED = 1;
        private static final int STATE_CANCELLED = 2;

        private static final int EXEC_IDLE = 0;
        private static final int EXEC_TICK = 1;
        private static final int EXEC_TASKS = 2;

        private final SchedulableTick tick;
        private final AtomicInteger scheduled = new AtomicInteger(STATE_NOT_SCHEDULED);
        private final AtomicInteger executing = new AtomicInteger(EXEC_IDLE);
        private final AtomicBoolean tasksNotified = new AtomicBoolean();

        private TickState(final SchedulableTick tick) {
            this.tick = tick;
        }

        private boolean tryMarkScheduled() {
            return this.scheduled.compareAndSet(STATE_NOT_SCHEDULED, STATE_SCHEDULED);
        }

        private boolean tryMarkCancelled() {
            return this.scheduled.compareAndSet(STATE_SCHEDULED, STATE_CANCELLED);
        }

        private boolean isScheduled() {
            return this.scheduled.get() == STATE_SCHEDULED;
        }

        private boolean isCancelled() {
            return this.scheduled.get() == STATE_CANCELLED;
        }

        private boolean tryEnterTick() {
            return this.executing.compareAndSet(EXEC_IDLE, EXEC_TICK);
        }

        private boolean tryEnterTasks() {
            return this.executing.compareAndSet(EXEC_IDLE, EXEC_TASKS);
        }

        private void exitExecution() {
            this.executing.set(EXEC_IDLE);
        }
    }
}
