package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

@Configurations(name = "dont_save_entity", type = ConfigurationType.optimisations)
public class DontSaveEntityConfig {
    @Configuration
    public static boolean dontSavePrimedTNT = false;
    @Configuration
    public static boolean dontSaveFallingBlock = false;
}
