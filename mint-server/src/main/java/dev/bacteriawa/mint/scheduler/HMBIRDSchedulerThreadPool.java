package dev.bacteriawa.mint.scheduler;

import ca.spottedleaf.concurrentutil.scheduler.SchedulableTick;
import ca.spottedleaf.concurrentutil.scheduler.Scheduler;
import ca.spottedleaf.concurrentutil.scheduler.SchedulerAccess;
import ca.spottedleaf.common.util.TimeUtil;
import com.mojang.logging.LogUtils;
import io.papermc.paper.threadedregions.TickRegionScheduler;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import org.slf4j.Logger;

public final class HMBIRDSchedulerThreadPool extends Scheduler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long IDLE_PARK_NANOS = TimeUnit.MILLISECONDS.toNanos(1L);
    private static final long DEFAULT_SOFT_WATERMARK_AUTOSCALE_NANOS = TimeUnit.SECONDS.toNanos(15L);
    private static final long DEFAULT_AUTOSCALE_COOLDOWN_NANOS = TimeUnit.SECONDS.toNanos(15L);

    private final ThreadFactory threadFactory;
    private final AtomicBoolean halted = new AtomicBoolean();
    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicInteger nextWorkerId = new AtomicInteger();

    private final Object workerLock = new Object();
    private final List<Worker> allWorkers = new ArrayList<>();
    private volatile Worker[] activeWorkers = new Worker[0];

    // Ordered by scheduled start time, then by tick id.
    private final PriorityQueue<TickState> scheduledTicks = new PriorityQueue<>((a, b) -> {
        final int cmp = TimeUtil.compareTimes(SchedulerAccess.getScheduledStart(a.tick), SchedulerAccess.getScheduledStart(b.tick));
        if (cmp != 0) {
            return cmp;
        }
        return Long.compare(a.tick.id, b.tick.id);
    });
    // Guards scheduledTicks and timer thread coordination.
    private final Object scheduleLock = new Object();
    private final Thread timerThread;

    // Global queue for ready tasks, local queues for affinity/steal.
    private final HMBIRDDispatchQueue globalQueue = new HMBIRDDispatchQueue();
    private final HMBIRDMetrics metrics = new HMBIRDMetrics();

    // Count of intermediate tasks currently enqueued.
    private final AtomicLong pendingIntermediate = new AtomicLong();

    // Time budget for intermediate tasks per tick.
    private volatile long intermediateTimeSliceNs = TimeUnit.MILLISECONDS.toNanos(2L);
    // No-drop soft watermarks for pending intermediate tasks.
    private volatile long rejectGlobalQueueSize = 200_000L;
    private volatile long recoverGlobalQueueSize = 100_000L;
    private final AtomicBoolean softRejectStatus = new AtomicBoolean();
    private final AtomicLong softRejectSinceNanos = new AtomicLong(TimeUtil.DEADLINE_NOT_SET);
    private volatile long softWatermarkAutoscaleNanos = DEFAULT_SOFT_WATERMARK_AUTOSCALE_NANOS;
    private volatile long autoScaleCooldownNanos = DEFAULT_AUTOSCALE_COOLDOWN_NANOS;
    private final AtomicLong lastAutoScaleNanos = new AtomicLong(TimeUtil.DEADLINE_NOT_SET);
    private volatile int autoScaleMaxThreads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);

    public HMBIRDSchedulerThreadPool(final int threadCount, final ThreadFactory threadFactory) {
        this.threadFactory = Objects.requireNonNull(threadFactory, "threadFactory");
        this.timerThread = new Thread(this::timerLoop, "HMBIRD Scheduler Timer");
        this.timerThread.setDaemon(true);
        this.setThreads(Math.max(1, threadCount));
    }

    public void start() {
        if (!this.started.compareAndSet(false, true)) {
            return;
        }
        this.timerThread.start();
        synchronized (this.workerLock) {
            for (final Worker worker : this.allWorkers) {
                if (!worker.retiring || !worker.localQueue.isEmpty()) {
                    worker.startIfNeeded();
                }
            }
        }
    }

    public void setThreads(final int threads) {
        final int targetThreads = Math.max(1, threads);
        final Worker[] startAfterUnlock;
        synchronized (this.workerLock) {
            final Worker[] current = this.activeWorkers;
            if (targetThreads == current.length) {
                return;
            }

            if (targetThreads > current.length) {
                final Worker[] next = new Worker[targetThreads];
                System.arraycopy(current, 0, next, 0, current.length);
                final List<Worker> created = new ArrayList<>(targetThreads - current.length);
                for (int i = current.length; i < targetThreads; ++i) {
                    final Worker worker = this.createWorker();
                    next[i] = worker;
                    created.add(worker);
                }
                this.activeWorkers = next;
                startAfterUnlock = created.toArray(new Worker[0]);
            } else {
                final Worker[] next = new Worker[targetThreads];
                System.arraycopy(current, 0, next, 0, targetThreads);
                for (int i = targetThreads; i < current.length; ++i) {
                    current[i].retire();
                }
                this.activeWorkers = next;
                startAfterUnlock = new Worker[0];
            }
        }

        if (this.started.get()) {
            for (final Worker worker : startAfterUnlock) {
                worker.startIfNeeded();
            }
        }
        this.wakeWorkers();
        LOGGER.info("HMBIRD scheduler worker target changed to " + targetThreads + " active workers");
    }

    public void setIntermediateTimeSliceNs(final long sliceNs) {
        this.intermediateTimeSliceNs = Math.max(0L, sliceNs);
    }

    public void setSoftWatermarkThresholds(final long rejectSize, final long recoverSize) {
        this.rejectGlobalQueueSize = Math.max(0L, rejectSize);
        this.recoverGlobalQueueSize = Math.max(0L, Math.min(recoverSize, this.rejectGlobalQueueSize));
        this.updateSoftWatermark(this.pendingIntermediate.get());
    }

    public void setQueueRejectThresholds(final long rejectSize, final long recoverSize) {
        this.setSoftWatermarkThresholds(rejectSize, recoverSize);
    }

    public HMBIRDMetrics.Snapshot getMetricsSnapshot() {
        this.updateSoftWatermark(this.pendingIntermediate.get());
        int retiring = 0;
        long localQueueSize = 0L;
        synchronized (this.workerLock) {
            for (final Worker worker : this.allWorkers) {
                localQueueSize += worker.localQueue.size();
                if (worker.retiring && worker.isAlive()) {
                    ++retiring;
                }
            }
        }
        final boolean softReject = this.softRejectStatus.get();
        final long since = this.softRejectSinceNanos.get();
        final long activeNanos = softReject && since != TimeUtil.DEADLINE_NOT_SET ? Math.max(0L, System.nanoTime() - since) : 0L;
        return this.metrics.snapshot(
            this.activeWorkers.length,
            retiring,
            this.globalQueue.size(),
            localQueueSize,
            this.pendingIntermediate.get(),
            softReject,
            activeNanos,
            this.rejectGlobalQueueSize,
            this.recoverGlobalQueueSize,
            this.autoScaleMaxThreads
        );
    }

    void setSoftWatermarkAutoscaleDelayNsForTesting(final long nanos) {
        this.softWatermarkAutoscaleNanos = Math.max(0L, nanos);
    }

    void setAutoScaleCooldownNsForTesting(final long nanos) {
        this.autoScaleCooldownNanos = Math.max(0L, nanos);
    }

    void setAutoScaleMaxThreadsForTesting(final int threads) {
        this.autoScaleMaxThreads = Math.max(1, threads);
    }

    @Override
    public Thread[] getAliveThreads() {
        final List<Thread> alive = new ArrayList<>();
        synchronized (this.workerLock) {
            for (final Worker worker : this.allWorkers) {
                final Thread thread = worker.thread;
                if (thread.isAlive()) {
                    alive.add(thread);
                }
            }
        }
        if (this.timerThread.isAlive()) {
            alive.add(this.timerThread);
        }
        return alive.toArray(new Thread[0]);
    }

    @Override
    public Thread[] getCoreThreads() {
        final Worker[] active = this.activeWorkers;
        final Thread[] ret = new Thread[active.length];
        for (int i = 0; i < active.length; ++i) {
            ret[i] = active[i].thread;
        }
        return ret;
    }

    @Override
    public void halt() {
        if (!this.halted.compareAndSet(false, true)) {
            return;
        }
        this.wakeAllWorkers();
        LockSupport.unpark(this.timerThread);
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
        while (true) {
            final Thread[] alive = this.getAliveThreads();
            if (alive.length == 0) {
                return true;
            }
            boolean joinedOtherThread = false;
            for (final Thread thread : alive) {
                if (thread == Thread.currentThread()) {
                    continue;
                }
                joinedOtherThread = true;
                if (msToWait <= 0L) {
                    thread.join();
                    continue;
                }
                final long now = System.nanoTime();
                if (now - deadlineNs >= 0L) {
                    return false;
                }
                thread.join(Math.max(1L, TimeUnit.NANOSECONDS.toMillis(deadlineNs - now)));
            }
            if (!joinedOtherThread) {
                return true;
            }
        }
    }

    @Override
    public void schedule(final SchedulableTick tick) {
        if (SchedulerAccess.getScheduledStart(tick) == TimeUtil.DEADLINE_NOT_SET) {
            throw new IllegalStateException("Task start is not set");
        }
        if (this.halted.get()) {
            throw new IllegalStateException("Scheduler halted");
        }

        final TickState state = new TickState(tick);
        if (!SchedulerAccess.setState(tick, state)) {
            throw new IllegalStateException("Task " + tick + " is already scheduled or cancelled");
        }

        synchronized (this.scheduleLock) {
            if (this.halted.get()) {
                state.tryMarkCancelled();
                return;
            }
            if (state.tryMarkScheduled()) {
                this.metrics.onSchedule();
                this.scheduledTicks.add(state);
                this.scheduleLock.notifyAll();
            }
        }

        if (tick.hasTasks()) {
            this.notifyTasks(tick);
        }
    }

    @Override
    public void notifyTasks(final SchedulableTick tick) {
        final TickState state = (TickState)SchedulerAccess.getState(tick);
        if (state == null || !state.isScheduled() || this.halted.get()) {
            return;
        }
        this.metrics.onNotifyTasks();

        final long now = System.nanoTime();
        final TaskNotificationResult result = state.tryScheduleTasks(now);
        if (result == TaskNotificationResult.CANCELLED) {
            return;
        }
        if (result == TaskNotificationResult.COALESCED) {
            this.metrics.onCoalescedNotification();
            return;
        }

        final long pending = this.pendingIntermediate.incrementAndGet();
        this.updateSoftWatermark(pending);

        final long enqueueNanos = state.taskFirstEnqueueNanos();
        final HMBIRDSchedProp prop = new HMBIRDSchedProp(HMBIRDSchedProp.DEADLINE_LEVEL2, this.affinityDomain(tick), false, true);
        final HMBIRDTask task = new HMBIRDTask(() -> this.runIntermediateTaskNotification(state, enqueueNanos), prop, enqueueNanos, now);
        if (!this.offer(task)) {
            this.pendingIntermediate.decrementAndGet();
            state.finishTaskDispatch(true);
            this.updateSoftWatermark(this.pendingIntermediate.get());
            return;
        }
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
        state.cancelPendingTasks();
        this.metrics.onCancelled();
        this.wakeWorkers();
        synchronized (this.scheduleLock) {
            this.scheduleLock.notifyAll();
        }
        return true;
    }

    private Worker createWorker() {
        final Worker worker = new Worker(this.nextWorkerId.getAndIncrement());
        worker.thread = this.threadFactory.newThread(worker);
        this.allWorkers.add(worker);
        return worker;
    }

    private void wakeWorkers() {
        final Worker[] active = this.activeWorkers;
        for (final Worker worker : active) {
            LockSupport.unpark(worker.thread);
        }
    }

    private void wakeAllWorkers() {
        synchronized (this.workerLock) {
            for (final Worker worker : this.allWorkers) {
                LockSupport.unpark(worker.thread);
            }
        }
    }

    private int affinityDomain(final SchedulableTick tick) {
        if (tick instanceof TickRegionScheduler.RegionScheduleHandle handle && handle.region != null) {
            return Long.hashCode(handle.region.id);
        }
        return Long.hashCode(tick.id);
    }

    private boolean offer(final HMBIRDTask task) {
        if (this.halted.get()) {
            return false;
        }

        final long now = System.nanoTime();
        if (TimeUtil.compareTimes(task.scheduledNanos(), now) > 0) {
            this.globalQueue.offer(task);
            return true;
        }

        if (task.prop().deadlineLevel() >= HMBIRDSchedProp.DEADLINE_LEVEL2) {
            final Worker worker = this.selectWorker(task.prop().affinityDomain());
            if (worker != null) {
                worker.localQueue.offer(task);
                LockSupport.unpark(worker.thread);
                return true;
            }
        }

        this.globalQueue.offer(task);
        return true;
    }

    private Worker selectWorker(final int affinityDomain) {
        final Worker[] active = this.activeWorkers;
        if (active.length == 0) {
            return null;
        }
        return active[Math.floorMod(affinityDomain, active.length)];
    }

    private void timerLoop() {
        while (!this.halted.get()) {
            this.updateSoftWatermark(this.pendingIntermediate.get());
            TickState due = null;
            synchronized (this.scheduleLock) {
                while (!this.halted.get()) {
                    final TickState next = this.scheduledTicks.peek();
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
                        due = this.scheduledTicks.poll();
                        break;
                    }
                    final long waitNs = scheduledStart - now;
                    try {
                        this.scheduleLock.wait(Math.max(1L, Math.min(1_000L, TimeUnit.NANOSECONDS.toMillis(waitNs))));
                    } catch (final InterruptedException ignored) {
                    }
                }
                if (this.halted.get()) {
                    return;
                }
            }

            if (due == null || !due.isScheduled()) {
                continue;
            }
            this.enqueueDueTick(due);
        }
    }

    private void enqueueDueTick(final TickState due) {
        if (!due.tryQueueTick()) {
            return;
        }
        final long enqueue = System.nanoTime();
        final long scheduledStart = SchedulerAccess.getScheduledStart(due.tick);
        final HMBIRDSchedProp prop = HMBIRDSchedProp.deadlineLevel3(this.affinityDomain(due.tick));
        final HMBIRDTask execTick = new HMBIRDTask(() -> this.runTickTask(due), prop, enqueue, scheduledStart);
        if (!this.offer(execTick)) {
            due.finishTickDispatch();
            return;
        }
        this.wakeWorkers();
    }

    private void runTickTask(final TickState state) {
        TickRunResult result = TickRunResult.NONE;
        try {
            if (this.halted.get() || state.isCancelled()) {
                return;
            }
            result = this.runTick(state);
        } finally {
            state.finishTickDispatch();
        }

        if (state.consumeDeferredTickIfIdle() && state.isScheduled() && !this.halted.get()) {
            this.enqueueDueTick(state);
        }
        if (result.rescheduleTimer()) {
            this.rescheduleTimer(state);
        }
        if (result.notifyTasks()) {
            this.notifyTasks(state.tick);
        }
    }

    private TickRunResult runTick(final TickState state) {
        if (!state.tryEnterTick()) {
            if (state.isScheduled() && !state.isCancelled() && !this.halted.get()) {
                state.deferTick();
            }
            return TickRunResult.NONE;
        }

        try {
            if (this.halted.get() || state.isCancelled()) {
                return TickRunResult.NONE;
            }
            final long scheduledStart = SchedulerAccess.getScheduledStart(state.tick);
            if (scheduledStart != TimeUtil.DEADLINE_NOT_SET) {
                this.metrics.onTickEnqueued(Math.max(0L, System.nanoTime() - scheduledStart));
            }

            final boolean reschedule = state.tick.runTick();
            this.metrics.onTickExecuted();
            if (!reschedule) {
                state.tryMarkCancelled();
                state.cancelPendingTasks();
                return TickRunResult.NONE;
            }

            if (state.isCancelled() || SchedulerAccess.getScheduledStart(state.tick) == TimeUtil.DEADLINE_NOT_SET) {
                state.tryMarkCancelled();
                state.cancelPendingTasks();
                return TickRunResult.NONE;
            }

            return state.tick.hasTasks() ? TickRunResult.RESCHEDULE_AND_NOTIFY : TickRunResult.RESCHEDULE;
        } finally {
            state.exitExecution();
        }
    }

    private void runIntermediateTaskNotification(final TickState state, final long enqueueNanos) {
        boolean requeue = false;
        try {
            if (!this.halted.get() && state.isScheduled()) {
                this.metrics.onIntermediateEnqueued(Math.max(0L, System.nanoTime() - enqueueNanos));
                this.runIntermediateTasks(state);
                this.metrics.onIntermediateExecuted();
            }
        } finally {
            this.pendingIntermediate.decrementAndGet();
            requeue = state.finishTaskDispatchAndCheckTasks(() -> !this.halted.get() && state.isScheduled() && state.tick.hasTasks());
            this.updateSoftWatermark(this.pendingIntermediate.get());
        }

        if (requeue) {
            this.metrics.onIntermediateRequeued();
            this.notifyTasks(state.tick);
        }
    }

    private void runIntermediateTasks(final TickState state) {
        if (!state.tryEnterTasks()) {
            return;
        }
        try {
            if (this.halted.get() || state.isCancelled() || !state.tick.hasTasks()) {
                return;
            }

            // Run until time slice or next scheduled tick deadline.
            final long start = System.nanoTime();
            final long sliceEnd = this.intermediateTimeSliceNs <= 0L ? start : start + this.intermediateTimeSliceNs;
            final long tickStart = SchedulerAccess.getScheduledStart(state.tick);
            final long deadline = tickStart == TimeUtil.DEADLINE_NOT_SET || TimeUtil.compareTimes(sliceEnd, tickStart) <= 0 ? sliceEnd : tickStart;

            final BooleanSupplier canContinue = () -> {
                final long now = System.nanoTime();
                return TimeUtil.compareTimes(now, deadline) < 0 && !state.isCancelled() && !this.halted.get();
            };
            final boolean keep = state.tick.runTasks(canContinue);
            if (!keep) {
                state.tryMarkCancelled();
                state.cancelPendingTasks();
            }
        } finally {
            state.exitExecution();
            if (state.consumeDeferredTickIfIdle() && state.isScheduled() && !this.halted.get()) {
                this.enqueueDueTick(state);
            }
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

    private HMBIRDTask pollWork(final Worker worker) {
        final HMBIRDTask local = worker.localQueue.pollAny();
        if (local != null) {
            return local;
        }

        if (worker.retiring) {
            return null;
        }

        final long now = this.halted.get() ? Long.MAX_VALUE : System.nanoTime();
        final HMBIRDTask ready = this.globalQueue.pollReady(now);
        if (ready != null) {
            return ready;
        }

        final Worker[] active = this.activeWorkers;
        if (active.length > 1) {
            final int start = Math.floorMod(worker.id, active.length);
            for (int i = 1; i < active.length; ++i) {
                final Worker victim = active[(start + i) % active.length];
                if (victim == worker || victim.retiring) {
                    continue;
                }
                final HMBIRDTask stolen = victim.localQueue.stealAny();
                if (stolen != null) {
                    return stolen;
                }
            }
        }

        return null;
    }

    private void updateSoftWatermark(final long pending) {
        final long high = this.rejectGlobalQueueSize;
        if (high <= 0L) {
            this.clearSoftWatermark();
            return;
        }

        final long low = Math.min(this.recoverGlobalQueueSize, high);
        final long now = System.nanoTime();
        if (pending >= high) {
            if (this.softRejectStatus.compareAndSet(false, true)) {
                this.softRejectSinceNanos.set(now);
                this.metrics.onSoftWatermarkActivated();
                LOGGER.warn("HMBIRD pending intermediate tasks reached soft high watermark: pending=" + pending + ", high=" + high + ", low=" + low);
            }
            this.maybeAutoScale(now, pending);
            return;
        }

        if (pending <= low) {
            if (this.softRejectStatus.compareAndSet(true, false)) {
                this.softRejectSinceNanos.set(TimeUtil.DEADLINE_NOT_SET);
                LOGGER.info("HMBIRD pending intermediate tasks recovered below soft low watermark: pending=" + pending + ", high=" + high + ", low=" + low);
            }
        }
    }

    private void clearSoftWatermark() {
        if (this.softRejectStatus.compareAndSet(true, false)) {
            this.softRejectSinceNanos.set(TimeUtil.DEADLINE_NOT_SET);
        }
    }

    private void maybeAutoScale(final long now, final long pending) {
        final long since = this.softRejectSinceNanos.get();
        if (since == TimeUtil.DEADLINE_NOT_SET || TimeUtil.compareTimes(now, since + this.softWatermarkAutoscaleNanos) < 0) {
            return;
        }

        final Worker[] active = this.activeWorkers;
        final int currentThreads = active.length;
        final int maxThreads = Math.max(1, this.autoScaleMaxThreads);
        if (currentThreads >= maxThreads) {
            return;
        }

        final long last = this.lastAutoScaleNanos.get();
        if (last != TimeUtil.DEADLINE_NOT_SET && TimeUtil.compareTimes(now, last + this.autoScaleCooldownNanos) < 0) {
            return;
        }
        if (!this.lastAutoScaleNanos.compareAndSet(last, now)) {
            return;
        }

        final int nextThreads = Math.min(maxThreads, currentThreads + 1);
        this.setThreads(nextThreads);
        this.softRejectSinceNanos.set(now);
        this.metrics.onAutoScale();
        LOGGER.warn(
            "HMBIRD soft watermark sustained; auto-scaled workers from " + currentThreads + " to " + nextThreads
                + " (pendingIntermediate=" + pending + ", globalQueue=" + this.globalQueue.size()
                + ", softHigh=" + this.rejectGlobalQueueSize + ", softLow=" + this.recoverGlobalQueueSize + ")"
        );
    }

    private enum TaskNotificationResult {
        ENQUEUED,
        COALESCED,
        CANCELLED
    }

    private record TickRunResult(boolean rescheduleTimer, boolean notifyTasks) {
        private static final TickRunResult NONE = new TickRunResult(false, false);
        private static final TickRunResult RESCHEDULE = new TickRunResult(true, false);
        private static final TickRunResult RESCHEDULE_AND_NOTIFY = new TickRunResult(true, true);
    }

    private final class Worker implements Runnable {

        private final int id;
        private final HMBIRDLocalQueue localQueue = new HMBIRDLocalQueue();
        private final AtomicBoolean started = new AtomicBoolean();
        private volatile boolean retiring;
        private Thread thread;

        private Worker(final int id) {
            this.id = id;
        }

        private boolean isAlive() {
            return this.thread.isAlive();
        }

        private void startIfNeeded() {
            if (this.started.compareAndSet(false, true)) {
                this.thread.start();
            }
        }

        private void retire() {
            this.retiring = true;
            LockSupport.unpark(this.thread);
        }

        @Override
        public void run() {
            while (true) {
                final HMBIRDTask task = HMBIRDSchedulerThreadPool.this.pollWork(this);
                if (task != null) {
                    task.run();
                    continue;
                }
                if (this.retiring && this.localQueue.isEmpty()) {
                    return;
                }
                if (HMBIRDSchedulerThreadPool.this.halted.get()) {
                    return;
                }
                LockSupport.parkNanos(IDLE_PARK_NANOS);
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
        private final AtomicBoolean tasksQueued = new AtomicBoolean();
        private final AtomicBoolean tickQueued = new AtomicBoolean();
        private final AtomicBoolean deferredTick = new AtomicBoolean();
        private final AtomicLong taskFirstEnqueueNanos = new AtomicLong(TimeUtil.DEADLINE_NOT_SET);
        private final Object taskNotifyLock = new Object();

        private TickState(final SchedulableTick tick) {
            this.tick = tick;
        }

        private boolean tryMarkScheduled() {
            return this.scheduled.compareAndSet(STATE_NOT_SCHEDULED, STATE_SCHEDULED);
        }

        private boolean tryMarkCancelled() {
            while (true) {
                final int current = this.scheduled.get();
                if (current == STATE_CANCELLED) {
                    return false;
                }
                if (this.scheduled.compareAndSet(current, STATE_CANCELLED)) {
                    return current == STATE_SCHEDULED;
                }
            }
        }

        private boolean isScheduled() {
            return this.scheduled.get() == STATE_SCHEDULED;
        }

        private boolean isCancelled() {
            return this.scheduled.get() == STATE_CANCELLED;
        }

        private TaskNotificationResult tryScheduleTasks(final long now) {
            synchronized (this.taskNotifyLock) {
                if (!this.isScheduled()) {
                    return TaskNotificationResult.CANCELLED;
                }
                if (!this.tasksQueued.compareAndSet(false, true)) {
                    return TaskNotificationResult.COALESCED;
                }
                this.taskFirstEnqueueNanos.compareAndSet(TimeUtil.DEADLINE_NOT_SET, now);
                if (!this.isScheduled()) {
                    this.finishTaskDispatchLocked(true);
                    return TaskNotificationResult.CANCELLED;
                }
                return TaskNotificationResult.ENQUEUED;
            }
        }

        private long taskFirstEnqueueNanos() {
            final long enqueue = this.taskFirstEnqueueNanos.get();
            return enqueue == TimeUtil.DEADLINE_NOT_SET ? System.nanoTime() : enqueue;
        }

        private void finishTaskDispatch(final boolean resetEnqueueNanos) {
            synchronized (this.taskNotifyLock) {
                this.finishTaskDispatchLocked(resetEnqueueNanos);
            }
        }

        private boolean finishTaskDispatchAndCheckTasks(final BooleanSupplier shouldRequeue) {
            synchronized (this.taskNotifyLock) {
                this.tasksQueued.set(false);
                if (shouldRequeue.getAsBoolean()) {
                    return true;
                }
                this.resetTaskEnqueueNanos();
                return false;
            }
        }

        private void finishTaskDispatchLocked(final boolean resetEnqueueNanos) {
            if (resetEnqueueNanos) {
                this.resetTaskEnqueueNanos();
            }
            this.tasksQueued.set(false);
        }

        private void resetTaskEnqueueNanos() {
            this.taskFirstEnqueueNanos.set(TimeUtil.DEADLINE_NOT_SET);
        }

        private void cancelPendingTasks() {
            synchronized (this.taskNotifyLock) {
                this.resetTaskEnqueueNanos();
                this.tasksQueued.set(false);
            }
            this.tickQueued.set(false);
            this.deferredTick.set(false);
        }

        private boolean tryQueueTick() {
            return this.isScheduled() && this.tickQueued.compareAndSet(false, true);
        }

        private void finishTickDispatch() {
            this.tickQueued.set(false);
        }

        private void deferTick() {
            this.deferredTick.set(true);
        }

        private boolean consumeDeferredTickIfIdle() {
            return !this.tickQueued.get() && this.executing.get() == EXEC_IDLE && this.deferredTick.compareAndSet(true, false);
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
