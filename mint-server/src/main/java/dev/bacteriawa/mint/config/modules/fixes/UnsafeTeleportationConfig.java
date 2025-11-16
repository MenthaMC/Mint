package dev.bacteriawa.mint.config.modules.fixes;

import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

import static dev.bacteriawa.mint.config.ConfigurationType.fixes;

@Configurations(name = "unsafe_teleportation", type = fixes)
public class UnsafeTeleportationConfig {
    @Configuration
    public static boolean enabled = false;
}
