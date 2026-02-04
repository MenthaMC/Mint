package ca.spottedleaf.concurrentutil.scheduler;

/**
 * Internal accessors for scheduler package-private fields.
 */
public final class SchedulerAccess {

    private SchedulerAccess() {
    }

    public static long getScheduledStart(final SchedulableTick tick) {
        return tick.scheduledStart;
    }

    public static boolean setState(final SchedulableTick tick, final Object state) {
        return tick.setState(state);
    }

    public static Object getState(final SchedulableTick tick) {
        return tick.state;
    }
}
