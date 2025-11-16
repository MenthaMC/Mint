package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

@Configurations(name = "warn_on_offline_mode", type = ConfigurationType.misc)
public class OfflineModeWarningConfig {
    @Configuration
    public static boolean enabled = true;
}
