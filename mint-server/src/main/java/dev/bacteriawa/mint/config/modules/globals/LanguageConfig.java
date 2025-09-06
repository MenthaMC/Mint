package dev.bacteriawa.mint.config.modules.globals;

import me.coderfrish.mint.config.ConfigCategory;
import me.coderfrish.mint.config.annotation.Config;
import me.coderfrish.mint.config.annotation.ConfigField;

@Config(name = "language", category = ConfigCategory.globals)
public class LanguageConfig {
    @ConfigField
    public static String language = "en_us";
}