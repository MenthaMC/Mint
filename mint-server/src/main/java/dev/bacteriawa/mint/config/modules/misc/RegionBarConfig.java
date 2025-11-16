package dev.bacteriawa.mint.config.modules.misc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.commands.RegionBarCommand;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;
import dev.bacteriawa.mint.functions.GlobalServerRegionBar;
import org.bukkit.Bukkit;

import java.util.List;

@Configurations(name = "regionbar", type = ConfigurationType.misc)
public class RegionBarConfig {
    @Configuration
    public static boolean regionbarEnabled = true;
    @Configuration
    public static String regionBarFormat = "<gray>Util<yellow>:</yellow> <util> Chunks<yellow>:</yellow> <green><chunks></green> Players<yellow>:</yellow> <green><players></green> Entities<yellow>:</yellow> <green><entities></green>";
    @Configuration
    public static List<String> utilColors = List.of("GREEN", "YELLOW", "RED", "PURPLE");
    @Configuration
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