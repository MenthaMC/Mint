package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "secure_seed", type = ConfigCategory.misc)
public class SecureSeedConfig {
    @ConfigField(comment = {
            "Once you enable secure seed, all ores and structures are generated with 1024-bit seed",
            "instead of using 64-bit seed in vanilla, made seed cracker become impossible"
    })
    public static boolean enabled = false;
}
