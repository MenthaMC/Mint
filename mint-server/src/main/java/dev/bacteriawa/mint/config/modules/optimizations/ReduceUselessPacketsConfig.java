package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "reduce-entity-move-packets", type = ConfigCategory.optimisations)
public class ReduceUselessPacketsConfig {
    @ConfigField(comment = "Reduce useless entity move packets", commentZh = "减少无用的实体移动数据包")
    public static boolean reduceUselessEntityMovePackets = false;
}
