package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;

@Config(name = "unlimit_packet", category = ConfigCategory.optimisations)
public class UnlimitPacketConfig {
    @ConfigField
    public static boolean disablePacketLimit = true;
}
