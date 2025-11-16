package dev.bacteriawa.mint.config.modules.fixes;

import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

import static dev.bacteriawa.mint.config.ConfigurationType.fixes;

@Configurations(name = "use_vanilla_random_source", type = fixes)
public class VanillaRandomSourceConfig {
    @Configuration
    public static boolean enabled = false;
}
