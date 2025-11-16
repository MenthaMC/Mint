package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

@Configurations(name = "secure_seed", type = ConfigurationType.misc)
public class SecureSeedConfig {
    @Configuration
    public static boolean enabled = false;
}
