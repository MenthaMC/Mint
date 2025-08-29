package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "secure_seed", type = ConfigCategory.misc)
public class SecureSeedConfig {
    @ConfigField(comment = {
            "Once you enable secure seed, all ores and structures are generated with 1024-bit seed",
            "instead of using 64-bit seed in vanilla, made seed cracker become impossible."
    }, commentZh = {
            "一旦启用安全种子，所有矿物和结构都将使用 1024 位种子生成",
            "而不是原版中的 64 位种子，使种子破解变得不可能。"
    })
    public static boolean enabled = false;
}
