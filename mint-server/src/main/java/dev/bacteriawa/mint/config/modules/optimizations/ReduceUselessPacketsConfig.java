package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "reduce-entity-move-packets", type = ConfigCategory.optimisations)
public class ReduceUselessPacketsConfig {
    @ConfigField
    public static boolean reduceUselessEntityMovePackets = false;
}
