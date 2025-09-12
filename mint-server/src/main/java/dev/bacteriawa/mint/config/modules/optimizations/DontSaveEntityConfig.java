package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;

@Config(name = "dont_save_entity", category = ConfigCategory.optimisations)
public class DontSaveEntityConfig {
    @ConfigField
    public static boolean dontSavePrimedTNT = false;
    @ConfigField
    public static boolean dontSaveFallingBlock = false;
}
