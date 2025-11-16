package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

@Configurations(name = "verify_publickey_only_in_online_mode", type = ConfigurationType.misc)
public class PublickeyVerifyConfig {
    @Configuration
    public static boolean enabled = false;
}
