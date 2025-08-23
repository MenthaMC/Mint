package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "language", type = ConfigCategory.misc)
public class LanguageConfig {
    @ConfigField(comment = {
        "Please use the key from https://minecraft.wiki/w/Language",
        "Format example: en_us zh_cn"
    })
    public static String language = "en_us";
}