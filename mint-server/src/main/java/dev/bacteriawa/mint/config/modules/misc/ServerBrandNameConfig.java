package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

@Configurations(name = "server_brand_name", type = ConfigurationType.misc)
public class ServerBrandNameConfig {
    @Configuration
    public static String serverModName= "Mint";
}
