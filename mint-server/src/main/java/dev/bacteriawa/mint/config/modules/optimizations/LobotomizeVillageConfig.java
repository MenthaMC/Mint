package dev.bacteriawa.mint.config.modules.optimizations;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "lobotomize_villager", type = ConfigCategory.optimisations)
public class LobotomizeVillageConfig {
    @ConfigField(comment = "Enable villager lobotomization optimization", commentZh = "启用村民智力降低优化")
    public static boolean villagerLobotomizeEnabled = false;
    @ConfigField(comment = "check_interval", commentZh = "检查间隔")
    public static int villagerLobotomizeCheckInterval = 100;
    @ConfigField(comment = "wait_until_trade_locked", commentZh = "等待直到交易锁定")
    public static boolean villagerLobotomizeWaitUntilTradeLocked = false;
}