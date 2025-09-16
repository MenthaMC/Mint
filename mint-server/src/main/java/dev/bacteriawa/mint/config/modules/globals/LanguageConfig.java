package dev.bacteriawa.mint.config.modules.globals;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;

@Config(name = "language", category = ConfigCategory.globals)
public class LanguageConfig {
    @ConfigField
    public static String language = "en_us";
}