package dev.bacteriawa.mint.config.modules.globals;

import me.coderfrish.mint.config.ConfigCategory;
import me.coderfrish.mint.config.annotation.Config;
import me.coderfrish.mint.config.annotation.ConfigField;

@Config(name = "warn_logger", category = ConfigCategory.globals)
public class WarnLoggerConfig {
    @ConfigField
    public static boolean simdWarn = true;

    @ConfigField
    public static boolean vanillaCommandWarn = true;
}
