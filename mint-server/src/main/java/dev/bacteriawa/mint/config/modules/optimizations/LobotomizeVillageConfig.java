package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "lobotomize_villager", type = ConfigCategory.optimisations)
public class LobotomizeVillageConfig {
    @ConfigField
    public static boolean villagerLobotomizeEnabled = false;
    @ConfigField(comment = "check_interval")
    public static int villagerLobotomizeCheckInterval = 100;
    @ConfigField(comment = "wait_until_trade_locked")
    public static boolean villagerLobotomizeWaitUntilTradeLocked = false;
}