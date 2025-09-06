package dev.bacteriawa.mint.config.modules.misc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import me.coderfrish.mint.config.ConfigCategory;
import dev.bacteriawa.mint.commands.RegionBarCommand;
import dev.bacteriawa.mint.functions.GlobalServerRegionBar;
import me.coderfrish.mint.config.annotation.Config;
import me.coderfrish.mint.config.annotation.ConfigField;
import org.bukkit.Bukkit;

import java.util.List;

@Config(name = "regionbar", category = ConfigCategory.misc)
public class RegionBarConfig {
    @ConfigField
    public static boolean regionbarEnabled = true;
    @ConfigField
    public static String regionBarFormat = "<gray>Util<yellow>:</yellow> <util> Chunks<yellow>:</yellow> <green><chunks></green> Players<yellow>:</yellow> <green><players></green> Entities<yellow>:</yellow> <green><entities></green>";
    @ConfigField
    public static List<String> utilColors = List.of("GREEN", "YELLOW", "RED", "PURPLE");
    @ConfigField
    public static int updateInterval = 15;

    public static void loaded(CommentedFileConfig config) {
        if (regionbarEnabled) {
            GlobalServerRegionBar.init();
            Bukkit.getCommandMap().register("regionbar", "mint", new RegionBarCommand());
        } else {
            GlobalServerRegionBar.cancelBarUpdateTask();
        }
    }
}