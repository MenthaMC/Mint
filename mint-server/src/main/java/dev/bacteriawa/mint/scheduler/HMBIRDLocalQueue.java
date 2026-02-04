package dev.bacteriawa.mint.scheduler;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.LongAdder;

public final class HMBIRDLocalQueue {

    // Per-worker queues, LIFO for locality; higher levels are polled first.
    private final ConcurrentLinkedDeque<HMBIRDTask>[] byDeadlineLevel;
    // Approximate size across all levels.
    private final LongAdder size = new LongAdder();

    @SuppressWarnings("unchecked")
    public HMBIRDLocalQueue() {
        this.byDeadlineLevel = (ConcurrentLinkedDeque<HMBIRDTask>[])new ConcurrentLinkedDeque[4];
        for (int i = 0; i < this.byDeadlineLevel.length; ++i) {
            this.byDeadlineLevel[i] = new ConcurrentLinkedDeque<>();
        }
    }

    public long size() {
        return this.size.sum();
    }

    public void offer(final HMBIRDTask task) {
        if (!task.tryMarkQueued()) {
            return;
        }
        this.byDeadlineLevel[task.prop().deadlineLevel()].offerFirst(task);
        this.size.increment();
    }

    public HMBIRDTask pollAny() {
        // Prefer higher deadline levels.
        for (int level = this.byDeadlineLevel.length - 1; level >= 0; --level) {
            final ConcurrentLinkedDeque<HMBIRDTask> queue = this.byDeadlineLevel[level];
            final HMBIRDTask task = queue.pollFirst();
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

    public HMBIRDTask stealAny() {
        // Steal from tail to reduce contention and preserve locality.
        for (int level = this.byDeadlineLevel.length - 1; level >= 0; --level) {
            final ConcurrentLinkedDeque<HMBIRDTask> queue = this.byDeadlineLevel[level];
            final HMBIRDTask task = queue.pollLast();
            if (task == null) {
                continue;
            }
            this.size.decrement();
            if (task.prop().allowSteal() && task.tryMarkDispatching()) {
                return task;
            }
        }
        return null;
    }
}
