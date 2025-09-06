package dev.bacteriawa.mint.config.modules.optimizations;

import me.coderfrish.mint.config.ConfigCategory;
import me.coderfrish.mint.config.annotation.Config;
import me.coderfrish.mint.config.annotation.ConfigField;

@Config(name = "unlimit_packet", category = ConfigCategory.optimisations)
public class UnlimitPacketConfig {
    @ConfigField
    public static boolean disablePacketLimit = true;
}
