package dev.bacteriawa.mint.config.modules.fixes;

import me.coderfrish.mint.config.ConfigCategory;
import me.coderfrish.mint.config.annotation.Config;
import me.coderfrish.mint.config.annotation.ConfigField;

@Config(name = "use_vanilla_random_source", category = ConfigCategory.fixes)
public class VanillaRandomSourceConfig {
    @ConfigField
    public static boolean enabled = false;
}
