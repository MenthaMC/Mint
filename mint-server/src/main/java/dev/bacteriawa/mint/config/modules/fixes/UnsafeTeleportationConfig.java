package dev.bacteriawa.mint.config.modules.fixes;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "unsafe-teleportation", type = ConfigCategory.fixes)
public class UnsafeTeleportationConfig {
    @ConfigField(comment = {
            "If you want to use sand duping,please turn on this",
            "Warning: This would cause some unsafe issues, you could learn more on : https://github.com/PaperMC/Folia/issues/297"
    })
    public static boolean enabled = false;
}
