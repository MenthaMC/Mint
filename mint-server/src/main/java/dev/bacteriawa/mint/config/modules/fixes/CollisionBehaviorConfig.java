package dev.bacteriawa.mint.config.modules.fixes;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;

@Config(name = "collision_behavior", category = ConfigCategory.fixes)
public class CollisionBehaviorConfig {
    @ConfigField
    public static String behaviorMode = "VANILLA";
    @ConfigField
    public static boolean vanillaFluidPushing = true;
}
