package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

@Configurations(name = "unlimit_packet", type = ConfigurationType.optimisations)
public class UnlimitPacketConfig {
    @Configuration
    public static boolean disablePacketLimit = true;
}
