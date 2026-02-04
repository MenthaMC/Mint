package dev.bacteriawa.mint.scheduler;

public enum HMBIRDOpsState {
    // Task lifecycle states used by HMBIRDTask.
    NONE(0),
    QUEUEING(1),
    QUEUED(2),
    DISPATCHING(3);

    private final int code;

    HMBIRDOpsState(final int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static HMBIRDOpsState fromCode(final int code) {
        return switch (code) {
            case 0 -> NONE;
            case 1 -> QUEUEING;
            case 2 -> QUEUED;
            case 3 -> DISPATCHING;
            default -> throw new IllegalArgumentException("Unknown ops state code: " + code);
        };
    }
}
