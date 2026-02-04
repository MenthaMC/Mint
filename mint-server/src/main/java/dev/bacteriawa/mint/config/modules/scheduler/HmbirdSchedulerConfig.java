package dev.bacteriawa.mint.config.modules.scheduler;

import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

@Configurations(name = "hmbird_scheduler", type = ConfigurationType.scheduler)
public class HmbirdSchedulerConfig {
    @Configuration(alisa = "intermediate-time-slice-ms")
    public static long intermediateTimeSliceMs = 2L;

    @Configuration(alisa = "reject-global-queue-size")
    public static long rejectGlobalQueueSize = 200_000L;

    @Configuration(alisa = "recover-global-queue-size")
    public static long recoverGlobalQueueSize = 100_000L;
}
