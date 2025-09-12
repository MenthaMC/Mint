package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;

@Config(name = "server_brand_name", category = ConfigCategory.misc)
public class ServerBrandNameConfig {
    @ConfigField
    public static String serverModName= "Mint";
}
