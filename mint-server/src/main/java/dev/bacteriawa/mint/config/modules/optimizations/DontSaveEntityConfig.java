package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "dont-save-entity", type = ConfigCategory.optimisations)
public class DontSaveEntityConfig {
    @ConfigField(comment = "Disable save primed tnt")
    public static boolean dontSavePrimedTNT = false;
    @ConfigField(comment = "Not saving dropped cubes")
    public static boolean dontSaveFallingBlock = false;
}
