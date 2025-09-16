package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;

@Config(name = "suffocation_optimization", category = ConfigCategory.optimisations)
public class SuffocationOptimizationConfig {
    @ConfigField
    public static boolean enabled = false;
}
