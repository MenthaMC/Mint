package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;

@Config(name = "verify_publickey_only_in_online_mode", category = ConfigCategory.misc)
public class PublickeyVerifyConfig {
    @ConfigField
    public static boolean enabled = false;
}
