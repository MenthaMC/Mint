package dev.bacteriawa.mint.config.modules.fixes;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "vanilla-like_experience", type = ConfigCategory.fixes)
public class VanillaLikeExperienceConfig {
    @ConfigField(comment = {
            "As close to Vanilla as possible",
            "Need to open both UnsafeTeleportation to be effective sand duping"
    })
    public static boolean enabled = false;
}
