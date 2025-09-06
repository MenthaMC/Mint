package dev.bacteriawa.mint.config.modules.optimizations;

import me.coderfrish.mint.config.ConfigCategory;
import me.coderfrish.mint.config.annotation.Config;
import me.coderfrish.mint.config.annotation.ConfigField;

@Config(name = "reduce_sensor_work", category = ConfigCategory.optimisations)
public class PetalReduceSensorWorkConfig {
    @ConfigField
    public static boolean enabled = true;
    @ConfigField
    public static int delayTicks = 10;
}
