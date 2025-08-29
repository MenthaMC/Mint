package dev.bacteriawa.mint.config.modules.fixes;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "tripwire_dupe", type = ConfigCategory.fixes)
public class AllowTripwireDupeConfig {
    @ConfigField(comment = "Allow tripwire duplication", commentZh = "允许绊线复制")
    public static boolean enabled = false;
}

