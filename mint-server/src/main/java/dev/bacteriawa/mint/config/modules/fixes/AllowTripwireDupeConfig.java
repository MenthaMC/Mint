package dev.bacteriawa.mint.config.modules.fixes;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;

@Config(name = "tripwire_dupe", category = ConfigCategory.fixes)
public class AllowTripwireDupeConfig {
    @ConfigField
    public static boolean enabled = false;
}

