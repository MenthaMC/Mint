package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;

@Config(name = "username_checks", category = ConfigCategory.misc)
public class UsernameCheckConfig {
    @ConfigField
    public static boolean enabled = true;
}
