package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;

@Config(name = "warn_on_offline_mode", category = ConfigCategory.misc)
public class OfflineModeWarningConfig {
    @ConfigField
    public static boolean enabled = true;
}
