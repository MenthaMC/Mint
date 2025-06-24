package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "username_checks", type = ConfigCategory.misc)
public class UsernameCheckConfig {
    @ConfigField
    public static boolean enabled = true;
}
