package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

@Configurations(name = "reduce_sensor_work", type = ConfigurationType.optimisations)
public class PetalReduceSensorWorkConfig {
    @Configuration
    public static boolean enabled = true;
    @Configuration
    public static int delayTicks = 10;
}
