package dev.bacteriawa.mint.config.modules.misc;

import me.coderfrish.mint.config.ConfigCategory;
import me.coderfrish.mint.config.annotation.Config;
import me.coderfrish.mint.config.annotation.ConfigField;

@Config(name = "server_brand_name", category = ConfigCategory.misc)
public class ServerBrandNameConfig {
    @ConfigField
    public static String serverModName= "Mint";
}
