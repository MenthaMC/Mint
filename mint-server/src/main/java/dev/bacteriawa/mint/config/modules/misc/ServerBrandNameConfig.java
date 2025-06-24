package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "server-brand-name", type = ConfigCategory.misc)
public class ServerBrandNameConfig {
    @ConfigField
    public static String serverModName= "Mint";
}
