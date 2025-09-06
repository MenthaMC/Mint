package dev.bacteriawa.mint.config.modules.optimizations;

import me.coderfrish.mint.config.ConfigCategory;
import me.coderfrish.mint.config.annotation.Config;
import me.coderfrish.mint.config.annotation.ConfigField;

@Config(name = "reduce_entity_move_packets", category = ConfigCategory.optimisations)
public class ReduceUselessPacketsConfig {
    @ConfigField
    public static boolean reduceUselessEntityMovePackets = false;
}
