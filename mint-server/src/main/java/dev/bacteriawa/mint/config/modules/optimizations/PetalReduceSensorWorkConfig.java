package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "reduce_sensor_work", type = ConfigCategory.optimisations)
public class PetalReduceSensorWorkConfig {
    @ConfigField(comment = "Enable sensor work reduction optimization", commentZh = "启用传感器工作减少优化")
    public static boolean enabled = true;
    @ConfigField(comment = "Delay ticks for sensor work", commentZh = "传感器工作延迟刻数")
    public static int delayTicks = 10;
}
