package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

@Configurations(name = "dynamic_activation_brain", type = ConfigurationType.optimisations)
public class DynamicActivationBrainConfig {
    @Configuration
    public static boolean enabled = false;
    @Configuration
    public static int startDistance = 12;
    @Configuration
    public static int activationDistanceMod = 8;
    @Configuration
    public static int maximumActivationPrio = 20;
}
