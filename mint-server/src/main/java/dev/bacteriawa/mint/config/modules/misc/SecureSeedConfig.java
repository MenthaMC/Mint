package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;

@Config(name = "secure_seed", category = ConfigCategory.misc)
public class SecureSeedConfig {
    @ConfigField
    public static boolean enabled = false;
}
