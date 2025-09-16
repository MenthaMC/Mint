package dev.bacteriawa.mint.config.modules.fixes;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;

@Config(name = "unsafe_teleportation", category = ConfigCategory.fixes)
public class UnsafeTeleportationConfig {
    @ConfigField
    public static boolean enabled = false;
}
