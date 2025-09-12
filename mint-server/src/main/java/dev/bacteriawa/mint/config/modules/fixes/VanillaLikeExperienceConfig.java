package dev.bacteriawa.mint.config.modules.fixes;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;

@Config(name = "vanilla_like_experience", category = ConfigCategory.fixes)
public class VanillaLikeExperienceConfig {
    @ConfigField
    public static boolean enabled = false;
}
