package dev.bacteriawa.mint.config.modules.fixes;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;

@Config(name = "use_vanilla_random_source", category = ConfigCategory.fixes)
public class VanillaRandomSourceConfig {
    @ConfigField
    public static boolean enabled = false;
}
