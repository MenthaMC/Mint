package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "suffocation_optimization", type = ConfigCategory.optimisations)
public class SuffocationOptimizationConfig {
    @ConfigField(comment = "Enable suffocation optimization", commentZh = "启用窒息优化")
    public static boolean enabled = false;
}
