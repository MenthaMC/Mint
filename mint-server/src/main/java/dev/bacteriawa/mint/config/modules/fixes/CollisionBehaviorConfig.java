package dev.bacteriawa.mint.config.modules.fixes;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "Collision Behavior", type = ConfigCategory.fixes)
public class CollisionBehaviorConfig {
    @ConfigField(comment = {"Available Value: ", "VANILLA", "BLOCK_SHAPE_VANILLA", "PAPER"}, commentZh = {"可用值：", "VANILLA", "BLOCK_SHAPE_VANILLA", "PAPER"})
    public static String behaviorMode = "VANILLA";
    @ConfigField(comment = "Enable vanilla fluid pushing behavior", commentZh = "启用原版液体推送行为")
    public static boolean vanillaFluidPushing = true;
}
