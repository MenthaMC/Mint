package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "dont-save-entity", type = ConfigCategory.optimisations)
public class DontSaveEntityConfig {
    @ConfigField(comment = "Disable save primed tnt on chunk unloads.", commentZh = "禁用在区块卸载时保存点燃的TNT")
    public static boolean dontSavePrimedTNT = false;
    @ConfigField(comment = "Not saving dropped cubes.", commentZh = "不保存掉落方块")
    public static boolean dontSaveFallingBlock = false;
}
