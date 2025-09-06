package dev.bacteriawa.mint.config.modules.fixes;

import me.coderfrish.mint.config.ConfigCategory;
import me.coderfrish.mint.config.annotation.Config;
import me.coderfrish.mint.config.annotation.ConfigField;

@Config(name = "tripwire_dupe", category = ConfigCategory.fixes)
public class AllowTripwireDupeConfig {
    @ConfigField
    public static boolean enabled = false;
}

