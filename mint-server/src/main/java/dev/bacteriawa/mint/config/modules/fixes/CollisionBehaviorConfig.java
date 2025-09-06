package dev.bacteriawa.mint.config.modules.fixes;

import me.coderfrish.mint.config.ConfigCategory;
import me.coderfrish.mint.config.annotation.Config;
import me.coderfrish.mint.config.annotation.ConfigField;

@Config(name = "collision_behavior", category = ConfigCategory.fixes)
public class CollisionBehaviorConfig {
    @ConfigField
    public static String behaviorMode = "VANILLA";
    @ConfigField
    public static boolean vanillaFluidPushing = true;
}
