package dev.bacteriawa.mint.scheduler;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public final class HMBIRDLatencySampler {

    // Ring buffer of recent samples (nanoseconds).
    private final long[] samples;
    private final AtomicInteger writeIndex = new AtomicInteger();
    private final AtomicInteger sampleCount = new AtomicInteger();

    public HMBIRDLatencySampler(final int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }
        this.samples = new long[capacity];
    }

    public void record(final long value) {
        // Overwrites oldest samples in a ring.
        final int idx = Math.floorMod(this.writeIndex.getAndIncrement(), this.samples.length);
        this.samples[idx] = value;
        this.sampleCount.updateAndGet(count -> Math.min(this.samples.length, count + 1));
    }

    public Snapshot snapshot() {
        final int count = Math.min(this.sampleCount.get(), this.samples.length);
        final long[] copy = Arrays.copyOf(this.samples, count);
        Arrays.sort(copy);
        return new Snapshot(copy);
    }

    public record Snapshot(long[] sortedNanos) {
        public int size() {
            return this.sortedNanos.length;
        }

        public long p50() {
            return percentile(0.50);
        }

        public long p95() {
            return percentile(0.95);
        }

        public long p99() {
            return percentile(0.99);
        }

        public long max() {
            return this.sortedNanos.length == 0 ? 0L : this.sortedNanos[this.sortedNanos.length - 1];
        }

        private long percentile(final double p) {
            if (this.sortedNanos.length == 0) {
                return 0L;
            }
            final int idx = (int)Math.min(this.sortedNanos.length - 1, Math.floor(p * (this.sortedNanos.length - 1)));
            return this.sortedNanos[idx];
        }
    }
}
