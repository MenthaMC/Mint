package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

@Configurations(name = "reduce_entity_move_packets", type = ConfigurationType.optimisations)
public class ReduceUselessPacketsConfig {
    @Configuration
    public static boolean reduceUselessEntityMovePackets = false;
}
