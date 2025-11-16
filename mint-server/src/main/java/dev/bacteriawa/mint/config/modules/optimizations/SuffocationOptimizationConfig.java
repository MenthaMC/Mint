package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

@Configurations(name = "suffocation_optimization", type = ConfigurationType.optimisations)
public class SuffocationOptimizationConfig {
    @Configuration
    public static boolean enabled = false;
}
