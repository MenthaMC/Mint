package dev.bacteriawa.mint.config.modules.globals;

import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

@Configurations(name = "language", type = ConfigurationType.globals)
public class LanguageConfig {
    @Configuration
    public static String language = "en_us";
}