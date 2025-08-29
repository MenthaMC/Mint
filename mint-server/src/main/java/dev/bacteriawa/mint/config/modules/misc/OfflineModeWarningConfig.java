package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "warn_on_offline_mode", type = ConfigCategory.misc)
public class OfflineModeWarningConfig {
    @ConfigField(comment = "Warn when server is in offline mode", commentZh = "当服务器处于离线模式时警告")
    public static boolean enabled = true;
}
