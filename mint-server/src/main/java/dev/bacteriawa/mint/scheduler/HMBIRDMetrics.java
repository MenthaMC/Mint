package dev.bacteriawa.mint.scheduler;

import java.util.concurrent.atomic.LongAdder;

public final class HMBIRDMetrics {

    private final LongAdder scheduledTicks = new LongAdder();
    private final LongAdder tickEnqueued = new LongAdder();
    private final LongAdder tickExecuted = new LongAdder();

    private final LongAdder notifyTasks = new LongAdder();
    private final LongAdder coalescedNotifications = new LongAdder();
    private final LongAdder intermediateEnqueued = new LongAdder();
    private final LongAdder intermediateExecuted = new LongAdder();
    private final LongAdder intermediateRequeued = new LongAdder();

    private final LongAdder softWatermarkActivations = new LongAdder();
    private final LongAdder autoScaleEvents = new LongAdder();
    private final LongAdder cancelled = new LongAdder();

    // Fixed-size latency samplers (nanoseconds).
    private final HMBIRDLatencySampler dueTickLatency = new HMBIRDLatencySampler(4096);
    private final HMBIRDLatencySampler intermediateLatency = new HMBIRDLatencySampler(4096);

    public void onSchedule() {
        this.scheduledTicks.increment();
    }

    public void onTickEnqueued(final long queueLatencyNanos) {
        this.tickEnqueued.increment();
        this.dueTickLatency.record(queueLatencyNanos);
    }

    public void onTickExecuted() {
        this.tickExecuted.increment();
    }

    public void onNotifyTasks() {
        this.notifyTasks.increment();
    }

    public void onCoalescedNotification() {
        this.coalescedNotifications.increment();
    }

    public void onIntermediateEnqueued(final long queueLatencyNanos) {
        this.intermediateEnqueued.increment();
        this.intermediateLatency.record(queueLatencyNanos);
    }

    public void onIntermediateExecuted() {
        this.intermediateExecuted.increment();
    }

    public void onIntermediateRequeued() {
        this.intermediateRequeued.increment();
    }

    public void onSoftWatermarkActivated() {
        this.softWatermarkActivations.increment();
    }

    public void onAutoScale() {
        this.autoScaleEvents.increment();
    }

    public void onCancelled() {
        this.cancelled.increment();
    }

    public Snapshot snapshot(
        final int activeWorkers,
        final int retiringWorkers,
        final long globalQueueSize,
        final long localQueueSize,
        final long pendingIntermediate,
        final boolean softRejectStatus,
        final long softRejectActiveNanos,
        final long rejectGlobalQueueSize,
        final long recoverGlobalQueueSize,
        final int autoScaleMaxThreads
    ) {
        final HMBIRDLatencySampler.Snapshot tickLatency = this.dueTickLatency.snapshot();
        final HMBIRDLatencySampler.Snapshot itLatency = this.intermediateLatency.snapshot();
        return new Snapshot(
            this.scheduledTicks.sum(),
            this.tickEnqueued.sum(),
            this.tickExecuted.sum(),
            this.notifyTasks.sum(),
            this.coalescedNotifications.sum(),
            this.intermediateEnqueued.sum(),
            this.intermediateExecuted.sum(),
            this.intermediateRequeued.sum(),
            this.softWatermarkActivations.sum(),
            this.autoScaleEvents.sum(),
            this.cancelled.sum(),
            activeWorkers,
            retiringWorkers,
            globalQueueSize,
            localQueueSize,
            pendingIntermediate,
            softRejectStatus,
            softRejectActiveNanos,
            rejectGlobalQueueSize,
            recoverGlobalQueueSize,
            autoScaleMaxThreads,
            tickLatency.p50(), tickLatency.p95(), tickLatency.p99(), tickLatency.max(),
            itLatency.p50(), itLatency.p95(), itLatency.p99(), itLatency.max()
        );
    }

    public record Snapshot(
        long scheduledTicks,
        long tickEnqueued,
        long tickExecuted,
        long notifyTasks,
        long coalescedNotifications,
        long intermediateEnqueued,
        long intermediateExecuted,
        long intermediateRequeued,
        long softWatermarkActivations,
        long autoScaleEvents,
        long cancelled,
        int activeWorkers,
        int retiringWorkers,
        long globalQueueSize,
        long localQueueSize,
        long pendingIntermediate,
        boolean softRejectStatus,
        long softRejectActiveNanos,
        long rejectGlobalQueueSize,
        long recoverGlobalQueueSize,
        int autoScaleMaxThreads,
        long dueTickLatencyP50Nanos,
        long dueTickLatencyP95Nanos,
        long dueTickLatencyP99Nanos,
        long dueTickLatencyMaxNanos,
        long intermediateLatencyP50Nanos,
        long intermediateLatencyP95Nanos,
        long intermediateLatencyP99Nanos,
        long intermediateLatencyMaxNanos
    ) {
    }
}
