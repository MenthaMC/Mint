package dev.bacteriawa.mint.config.modules.fixes;

import me.coderfrish.mint.config.ConfigCategory;
import me.coderfrish.mint.config.annotation.Config;
import me.coderfrish.mint.config.annotation.ConfigField;

@Config(name = "unsafe_teleportation", category = ConfigCategory.fixes)
public class UnsafeTeleportationConfig {
    @ConfigField
    public static boolean enabled = false;
}
