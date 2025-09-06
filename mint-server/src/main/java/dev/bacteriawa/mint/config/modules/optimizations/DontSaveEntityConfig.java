package dev.bacteriawa.mint.config.modules.optimizations;

import me.coderfrish.mint.config.ConfigCategory;
import me.coderfrish.mint.config.annotation.Config;
import me.coderfrish.mint.config.annotation.ConfigField;

@Config(name = "dont_save_entity", category = ConfigCategory.optimisations)
public class DontSaveEntityConfig {
    @ConfigField
    public static boolean dontSavePrimedTNT = false;
    @ConfigField
    public static boolean dontSaveFallingBlock = false;
}
