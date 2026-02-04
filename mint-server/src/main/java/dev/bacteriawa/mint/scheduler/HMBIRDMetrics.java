package dev.bacteriawa.mint.scheduler;

import java.util.concurrent.atomic.LongAdder;

public final class HMBIRDMetrics {

    private final LongAdder scheduledTicks = new LongAdder();
    private final LongAdder tickEnqueued = new LongAdder();
    private final LongAdder tickExecuted = new LongAdder();

    private final LongAdder notifyTasks = new LongAdder();
    private final LongAdder intermediateEnqueued = new LongAdder();
    private final LongAdder intermediateExecuted = new LongAdder();

    private final LongAdder rejected = new LongAdder();
    private final LongAdder cancelled = new LongAdder();

    // Fixed-size latency samplers (nanoseconds).
    private final HMBIRDLatencySampler tickQueueLatency = new HMBIRDLatencySampler(4096);
    private final HMBIRDLatencySampler intermediateQueueLatency = new HMBIRDLatencySampler(4096);

    public void onSchedule() {
        this.scheduledTicks.increment();
    }

    public void onTickEnqueued(final long queueLatencyNanos) {
        this.tickEnqueued.increment();
        this.tickQueueLatency.record(queueLatencyNanos);
    }

    public void onTickExecuted() {
        this.tickExecuted.increment();
    }

    public void onNotifyTasks() {
        this.notifyTasks.increment();
    }

    public void onIntermediateEnqueued(final long queueLatencyNanos) {
        this.intermediateEnqueued.increment();
        this.intermediateQueueLatency.record(queueLatencyNanos);
    }

    public void onIntermediateExecuted() {
        this.intermediateExecuted.increment();
    }

    public void onRejected() {
        this.rejected.increment();
    }

    public void onCancelled() {
        this.cancelled.increment();
    }

    public Snapshot snapshot(final long pendingIntermediate, final boolean droppingIntermediateTasks) {
        final HMBIRDLatencySampler.Snapshot tickLatency = this.tickQueueLatency.snapshot();
        final HMBIRDLatencySampler.Snapshot itLatency = this.intermediateQueueLatency.snapshot();
        return new Snapshot(
            this.scheduledTicks.sum(),
            this.tickEnqueued.sum(),
            this.tickExecuted.sum(),
            this.notifyTasks.sum(),
            this.intermediateEnqueued.sum(),
            this.intermediateExecuted.sum(),
            this.rejected.sum(),
            this.cancelled.sum(),
            pendingIntermediate,
            droppingIntermediateTasks,
            tickLatency.p50(), tickLatency.p95(), tickLatency.p99(), tickLatency.max(),
            itLatency.p50(), itLatency.p95(), itLatency.p99(), itLatency.max()
        );
    }

    public record Snapshot(
        long scheduledTicks,
        long tickEnqueued,
        long tickExecuted,
        long notifyTasks,
        long intermediateEnqueued,
        long intermediateExecuted,
        long rejected,
        long cancelled,
        long pendingIntermediate,
        boolean droppingIntermediateTasks,
        long tickQueueP50Nanos,
        long tickQueueP95Nanos,
        long tickQueueP99Nanos,
        long tickQueueMaxNanos,
        long intermediateQueueP50Nanos,
        long intermediateQueueP95Nanos,
        long intermediateQueueP99Nanos,
        long intermediateQueueMaxNanos
    ) {
    }
}
