package dev.bacteriawa.mint.config.modules.fixes;

import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;
import dev.bacteriawa.mint.enums.EnumCollisionBehavior;

@Configurations(name = "collision_behavior", type = ConfigurationType.fixes)
public class CollisionBehaviorConfig {
    @Configuration
    public static EnumCollisionBehavior behaviorMode = EnumCollisionBehavior.VANILLA;
    @Configuration
    public static boolean vanillaFluidPushing = true;
}
