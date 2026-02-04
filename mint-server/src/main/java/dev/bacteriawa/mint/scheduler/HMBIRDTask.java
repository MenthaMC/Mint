package dev.bacteriawa.mint.scheduler;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public final class HMBIRDTask {

    // Encodes (qseq, opsState) into a single long for atomic updates.
    private static final int OPS_STATE_BITS = 3;
    private static final long OPS_STATE_MASK = (1L << OPS_STATE_BITS) - 1L;
    private static final AtomicLong ID_GENERATOR = new AtomicLong();

    private final long id;
    private final Runnable runnable;
    private final HMBIRDSchedProp prop;
    private final long enqueueNanos;
    private final long scheduledNanos;
    private final AtomicLong state;

    public HMBIRDTask(final Runnable runnable, final HMBIRDSchedProp prop, final long enqueueNanos, final long scheduledNanos) {
        this.id = ID_GENERATOR.incrementAndGet();
        this.runnable = Objects.requireNonNull(runnable, "runnable");
        this.prop = Objects.requireNonNull(prop, "prop");
        this.enqueueNanos = enqueueNanos;
        this.scheduledNanos = scheduledNanos;
        this.state = new AtomicLong(encode(0L, HMBIRDOpsState.QUEUEING));
    }

    public long id() {
        return this.id;
    }

    public Runnable runnable() {
        return this.runnable;
    }

    public HMBIRDSchedProp prop() {
        return this.prop;
    }

    public long enqueueNanos() {
        return this.enqueueNanos;
    }

    public long scheduledNanos() {
        return this.scheduledNanos;
    }

    public long qseq() {
        return decodeQseq(this.state.get());
    }

    public HMBIRDOpsState opsState() {
        return HMBIRDOpsState.fromCode(decodeOpsState(this.state.get()));
    }

    public boolean tryTransition(final HMBIRDOpsState from, final HMBIRDOpsState to) {
        while (true) {
            final long prev = this.state.get();
            if (decodeOpsState(prev) != from.code()) {
                return false;
            }
            final long prevQseq = decodeQseq(prev);
            final long next = encode(prevQseq + 1L, to);
            if (this.state.compareAndSet(prev, next)) {
                return true;
            }
        }
    }

    public boolean tryMarkQueued() {
        return this.tryTransition(HMBIRDOpsState.QUEUEING, HMBIRDOpsState.QUEUED);
    }

    public boolean tryMarkDispatching() {
        return this.tryTransition(HMBIRDOpsState.QUEUED, HMBIRDOpsState.DISPATCHING);
    }

    public void run() {
        this.runnable.run();
    }

    private static long encode(final long qseq, final HMBIRDOpsState state) {
        return (qseq << OPS_STATE_BITS) | (state.code() & OPS_STATE_MASK);
    }

    private static long decodeQseq(final long value) {
        return value >>> OPS_STATE_BITS;
    }

    private static int decodeOpsState(final long value) {
        return (int)(value & OPS_STATE_MASK);
    }
}
