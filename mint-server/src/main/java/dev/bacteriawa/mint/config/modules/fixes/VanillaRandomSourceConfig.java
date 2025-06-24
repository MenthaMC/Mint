package dev.bacteriawa.mint.config.modules.fixes;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "use_vanilla_random_source", type = ConfigCategory.fixes)
public class VanillaRandomSourceConfig {
    @ConfigField(comment = "Related with RNG cracks")
    public static boolean enabled = false;
}
