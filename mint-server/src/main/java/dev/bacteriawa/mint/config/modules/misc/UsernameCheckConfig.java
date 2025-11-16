package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

@Configurations(name = "username_checks", type = ConfigurationType.misc)
public class UsernameCheckConfig {
    @Configuration
    public static boolean enabled = true;
}
