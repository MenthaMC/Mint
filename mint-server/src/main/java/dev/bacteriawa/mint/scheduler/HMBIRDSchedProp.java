package dev.bacteriawa.mint.scheduler;

public record HMBIRDSchedProp(
    int deadlineLevel,
    int affinityDomain,
    boolean boost,
    boolean allowSteal
) {

    // Deadline level 0..3 (higher means more urgent).
    public static final int DEADLINE_LEVEL0 = 0;
    public static final int DEADLINE_LEVEL1 = 1;
    public static final int DEADLINE_LEVEL2 = 2;
    public static final int DEADLINE_LEVEL3 = 3;

    public HMBIRDSchedProp {
        if (deadlineLevel < DEADLINE_LEVEL0 || deadlineLevel > DEADLINE_LEVEL3) {
            throw new IllegalArgumentException("deadlineLevel must be in [0, 3]");
        }
    }

    public static HMBIRDSchedProp defaultProp() {
        return new HMBIRDSchedProp(DEADLINE_LEVEL0, 0, false, true);
    }

    public static HMBIRDSchedProp deadlineLevel3(final int affinityDomain) {
        return new HMBIRDSchedProp(DEADLINE_LEVEL3, affinityDomain, true, true);
    }
}
