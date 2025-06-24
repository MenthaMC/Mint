package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "verify_publickey_only_in_online_mode", type = ConfigCategory.misc)
public class PublickeyVerifyConfig {
    @ConfigField
    public static boolean enabled = false;
}
