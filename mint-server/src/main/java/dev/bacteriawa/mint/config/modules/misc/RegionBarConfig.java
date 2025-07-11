package dev.bacteriawa.mint.config.modules.misc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;
import dev.bacteriawa.mint.commands.RegionBarCommand;
import dev.bacteriawa.mint.functions.GlobalServerRegionBar;
import org.bukkit.Bukkit;

import java.util.List;

@Configuration(name = "regionbar", type = ConfigCategory.misc)
public class RegionBarConfig {
    @ConfigField(comment = "enabled")
    public static boolean regionbarEnabled = true;
    @ConfigField(comment = "format")
    public static String regionBarFormat = "<gray>Util<yellow>:</yellow> <util> Chunks<yellow>:</yellow> <green><chunks></green> Players<yellow>:</yellow> <green><players></green> Entities<yellow>:</yellow> <green><entities></green>";
    @ConfigField(comment = "util_color_list")
    public static List<String> utilColors = List.of("GREEN", "YELLOW", "RED", "PURPLE");
    @ConfigField(comment = "update_interval_ticks")
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