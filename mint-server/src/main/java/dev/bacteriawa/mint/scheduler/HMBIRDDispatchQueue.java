package dev.bacteriawa.mint.scheduler;

import ca.spottedleaf.common.util.TimeUtil;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.LongAdder;

public final class HMBIRDDispatchQueue {

    // Global queues per deadline level (higher levels are polled first).
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
    public HMBIRDDispatchQueue() {
        this.byDeadlineLevel = (PriorityQueue<HMBIRDTask>[])new PriorityQueue[4];
        for (int i = 0; i < this.byDeadlineLevel.length; ++i) {
            this.byDeadlineLevel[i] = new PriorityQueue<>(TASK_ORDER);
        }
    }

    public long size() {
        return this.size.sum();
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
        return this.pollReady(Long.MAX_VALUE);
    }

    public HMBIRDTask pollReady(final long nowNanos) {
        // Only return tasks whose scheduled time has passed.
        for (int level = this.byDeadlineLevel.length - 1; level >= 0; --level) {
            final PriorityQueue<HMBIRDTask> queue = this.byDeadlineLevel[level];
            synchronized (queue) {
                while (true) {
                    final HMBIRDTask task = queue.peek();
                    if (task == null) {
                        break;
                    }
                    if (TimeUtil.compareTimes(task.scheduledNanos(), nowNanos) > 0) {
                        break;
                    }
                    final HMBIRDTask polled = queue.poll();
                    this.size.decrement();
                    if (polled.tryMarkDispatching()) {
                        return polled;
                    }
                }
            }
        }
        return null;
    }
}
