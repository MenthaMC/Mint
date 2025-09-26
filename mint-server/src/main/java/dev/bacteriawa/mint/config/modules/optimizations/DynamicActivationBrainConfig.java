package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;

@Config(name = "dynamic_activation_brain", category = ConfigCategory.optimisations)
public class DynamicActivationBrainConfig {
    @ConfigField
    public static boolean enabled = false;
    @ConfigField
    public static int startDistance = 12;
    @ConfigField
    public static int activationDistanceMod = 8;
    @ConfigField
    public static int maximumActivationPrio = 20;
}
