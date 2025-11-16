package dev.bacteriawa.mint.config.modules.fixes;

import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

import static dev.bacteriawa.mint.config.ConfigurationType.fixes;

@Configurations(name = "vanilla_like_experience", type = fixes)
public class VanillaLikeExperienceConfig {
    @Configuration
    public static boolean enabled = false;
}
