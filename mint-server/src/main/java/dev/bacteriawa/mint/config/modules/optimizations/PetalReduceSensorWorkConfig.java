package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "reduce_sensor_work", type = ConfigCategory.optimisations)
public class PetalReduceSensorWorkConfig {
    @ConfigField
    public static boolean enabled = true;
    @ConfigField
    public static int delayTicks = 10;
}
