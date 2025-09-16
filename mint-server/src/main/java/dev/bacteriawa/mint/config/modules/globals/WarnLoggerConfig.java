package dev.bacteriawa.mint.config.modules.globals;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;

@Config(name = "warn_logger", category = ConfigCategory.globals)
public class WarnLoggerConfig {
    @ConfigField
    public static boolean simdWarn = true;

    @ConfigField
    public static boolean vanillaCommandWarn = true;
}
