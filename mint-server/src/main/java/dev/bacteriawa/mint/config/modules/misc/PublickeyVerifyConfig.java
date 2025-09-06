package dev.bacteriawa.mint.config.modules.misc;

import me.coderfrish.mint.config.ConfigCategory;
import me.coderfrish.mint.config.annotation.Config;
import me.coderfrish.mint.config.annotation.ConfigField;

@Config(name = "verify_publickey_only_in_online_mode", category = ConfigCategory.misc)
public class PublickeyVerifyConfig {
    @ConfigField
    public static boolean enabled = false;
}
