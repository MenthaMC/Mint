package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;

@Config(name = "reduce_entity_move_packets", category = ConfigCategory.optimisations)
public class ReduceUselessPacketsConfig {
    @ConfigField
    public static boolean reduceUselessEntityMovePackets = false;
}
