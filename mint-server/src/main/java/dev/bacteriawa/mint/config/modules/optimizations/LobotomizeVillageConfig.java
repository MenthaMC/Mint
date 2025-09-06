package dev.bacteriawa.mint.config.modules.optimizations;

import me.coderfrish.mint.config.ConfigCategory;
import me.coderfrish.mint.config.annotation.Config;
import me.coderfrish.mint.config.annotation.ConfigField;

@Config(name = "lobotomize_villager", category = ConfigCategory.optimisations)
public class LobotomizeVillageConfig {
    @ConfigField
    public static boolean villagerLobotomizeEnabled = false;
    @ConfigField
    public static int villagerLobotomizeCheckInterval = 100;
    @ConfigField
    public static boolean villagerLobotomizeWaitUntilTradeLocked = false;
}