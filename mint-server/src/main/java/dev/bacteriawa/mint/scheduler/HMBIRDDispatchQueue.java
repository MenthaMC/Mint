package dev.bacteriawa.mint.scheduler;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.LongAdder;

public final class HMBIRDDispatchQueue {

    // Global queues per deadline level (higher levels are polled first).
    private final ConcurrentLinkedQueue<HMBIRDTask>[] byDeadlineLevel;
    // Approximate size across all levels.
    private final LongAdder size = new LongAdder();

    @SuppressWarnings("unchecked")
    public HMBIRDDispatchQueue() {
        this.byDeadlineLevel = (ConcurrentLinkedQueue<HMBIRDTask>[])new ConcurrentLinkedQueue[4];
        for (int i = 0; i < this.byDeadlineLevel.length; ++i) {
            this.byDeadlineLevel[i] = new ConcurrentLinkedQueue<>();
        }
    }

    public long size() {
        return this.size.sum();
    }

    public void offer(final HMBIRDTask task) {
        if (!task.tryMarkQueued()) {
            return;
        }
        this.byDeadlineLevel[task.prop().deadlineLevel()].offer(task);
        this.size.increment();
    }

    public HMBIRDTask pollAny() {
        // Prefer higher deadline levels.
        for (int level = this.byDeadlineLevel.length - 1; level >= 0; --level) {
            final ConcurrentLinkedQueue<HMBIRDTask> queue = this.byDeadlineLevel[level];
            final HMBIRDTask task = queue.poll();
            if (task == null) {
                continue;
            }
            this.size.decrement();
            if (task.tryMarkDispatching()) {
                return task;
            }
        }
        return null;
    }

    public HMBIRDTask pollReady(final long nowNanos) {
        // Only return tasks whose scheduled time has passed.
        for (int level = this.byDeadlineLevel.length - 1; level >= 0; --level) {
            final ConcurrentLinkedQueue<HMBIRDTask> queue = this.byDeadlineLevel[level];
            final HMBIRDTask task = queue.peek();
            if (task == null) {
                continue;
            }
            if (task.scheduledNanos() > nowNanos) {
                continue;
            }
            final HMBIRDTask polled = queue.poll();
            if (polled == null) {
                continue;
            }
            this.size.decrement();
            if (polled.tryMarkDispatching()) {
                return polled;
            }
        }
        return null;
    }
}
