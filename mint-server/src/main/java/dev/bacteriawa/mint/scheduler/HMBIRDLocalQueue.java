package dev.bacteriawa.mint.scheduler;

import ca.spottedleaf.concurrentutil.util.TimeUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.LongAdder;

public final class HMBIRDLocalQueue {

    // Per-worker queues; higher levels are polled first, then older due/enqueued tasks.
    private final PriorityQueue<HMBIRDTask>[] byDeadlineLevel;
    // Approximate size across all levels.
    private final LongAdder size = new LongAdder();
    private static final Comparator<HMBIRDTask> TASK_ORDER = (a, b) -> {
        int cmp = TimeUtil.compareTimes(a.scheduledNanos(), b.scheduledNanos());
        if (cmp != 0) {
            return cmp;
        }
        cmp = TimeUtil.compareTimes(a.enqueueNanos(), b.enqueueNanos());
        if (cmp != 0) {
            return cmp;
        }
        return Long.compare(a.id(), b.id());
    };

    @SuppressWarnings("unchecked")
    public HMBIRDLocalQueue() {
        this.byDeadlineLevel = (PriorityQueue<HMBIRDTask>[])new PriorityQueue[4];
        for (int i = 0; i < this.byDeadlineLevel.length; ++i) {
            this.byDeadlineLevel[i] = new PriorityQueue<>(TASK_ORDER);
        }
    }

    public long size() {
        return this.size.sum();
    }

    public boolean isEmpty() {
        return this.size() <= 0L;
    }

    public void offer(final HMBIRDTask task) {
        if (!task.tryMarkQueued()) {
            return;
        }
        final PriorityQueue<HMBIRDTask> queue = this.byDeadlineLevel[task.prop().deadlineLevel()];
        synchronized (queue) {
            queue.offer(task);
            this.size.increment();
        }
    }

    public HMBIRDTask pollAny() {
        // Prefer higher deadline levels.
        for (int level = this.byDeadlineLevel.length - 1; level >= 0; --level) {
            final PriorityQueue<HMBIRDTask> queue = this.byDeadlineLevel[level];
            synchronized (queue) {
                while (true) {
                    final HMBIRDTask task = queue.poll();
                    if (task == null) {
                        break;
                    }
                    this.size.decrement();
                    if (task.tryMarkDispatching()) {
                        return task;
                    }
                }
            }
        }
        return null;
    }

    public HMBIRDTask stealAny() {
        // Steal the oldest stealable task at the highest available deadline level.
        for (int level = this.byDeadlineLevel.length - 1; level >= 0; --level) {
            final PriorityQueue<HMBIRDTask> queue = this.byDeadlineLevel[level];
            synchronized (queue) {
                List<HMBIRDTask> skipped = null;
                try {
                    while (true) {
                        final HMBIRDTask task = queue.poll();
                        if (task == null) {
                            break;
                        }
                        this.size.decrement();
                        if (!task.prop().allowSteal()) {
                            if (skipped == null) {
                                skipped = new ArrayList<>();
                            }
                            skipped.add(task);
                            continue;
                        }
                        if (task.tryMarkDispatching()) {
                            return task;
                        }
                    }
                } finally {
                    if (skipped != null) {
                        for (final HMBIRDTask task : skipped) {
                            queue.offer(task);
                            this.size.increment();
                        }
                    }
                }
            }
        }
        return null;
    }
}
