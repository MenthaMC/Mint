package dev.bacteriawa.mint.config.modules.misc;

import me.coderfrish.mint.config.ConfigCategory;
import me.coderfrish.mint.config.annotation.Config;
import me.coderfrish.mint.config.annotation.ConfigField;

@Config(name = "secure_seed", category = ConfigCategory.misc)
public class SecureSeedConfig {
    @ConfigField
    public static boolean enabled = false;
}
