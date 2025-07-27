package dev.bacteriawa.mint.config.modules.misc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;
import dev.bacteriawa.mint.commands.NetworkBarCommand;
import dev.bacteriawa.mint.functions.GlobalServerNetworkBar;
import org.bukkit.Bukkit;

@Configuration(name = "networkbar", type = ConfigCategory.misc)
public class NetworkBarConfig {
    @ConfigField(comment = "enabled")
    public static boolean networkBarEnabled = true;
    @ConfigField(comment = "traffic_format")
    public static String trafficFormat = "<actionbar><#BF71FF>⬆ <green><outgoing-traffic>  <aqua><outgoing-pps>  <gray>|  <#6AFFF3>⬇ <green><incoming-traffic>  <aqua><incoming-pps>";
    @ConfigField(comment = "update_interval_ticks")
    public static int updateInterval = 15;

    public static void loaded(CommentedFileConfig config) {
        if (networkBarEnabled) {
            GlobalServerNetworkBar.init();
            Bukkit.getCommandMap().register("networkbar", "mint", new NetworkBarCommand());
        } else {
            GlobalServerNetworkBar.cancelBarUpdateTask();
        }
    }
}