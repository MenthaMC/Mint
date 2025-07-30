package dev.bacteriawa.mint.config.modules.fixes;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "Collision Behavior", type = ConfigCategory.fixes)
public class CollisionBehaviorConfig {
    @ConfigField(comment = {"Available Value: ", "VANILLA", "BLOCK_SHAPE_VANILLA", "PAPER"})
    public static String behaviorMode = "VANILLA";
    @ConfigField
    public static boolean vanillaFluidPushing = true;
}
