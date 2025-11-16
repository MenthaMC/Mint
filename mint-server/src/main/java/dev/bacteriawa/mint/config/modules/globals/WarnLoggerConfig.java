package dev.bacteriawa.mint.config.modules.globals;

import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

@Configurations(name = "warn_logger", type = ConfigurationType.globals)
public class WarnLoggerConfig {
    @Configuration
    public static boolean simdWarn = true;

    @Configuration
    public static boolean vanillaCommandWarn = true;
}
