package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "warn_on_offline_mode", type = ConfigCategory.misc)
public class OfflineModeWarningConfig {
    @ConfigField
    public static boolean enabled = true;
}
