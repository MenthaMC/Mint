package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "UnlimitPacket", type = ConfigCategory.optimisations)
public class UnlimitPacketConfig {
    @ConfigField(comment = "Disable packet limit", commentZh = "禁用数据包限制")
    public static boolean disablePacketLimit = true;
}
