package dev.bacteriawa.mint.config.modules.misc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.commands.NetworkBarCommand;
import dev.bacteriawa.mint.functions.GlobalServerNetworkBar;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;
import org.bukkit.Bukkit;

@Config(name = "networkbar", category = ConfigCategory.misc)
public class NetworkBarConfig {
    @ConfigField
    public static boolean networkBarEnabled = true;
    @ConfigField
    public static String trafficFormat = "<actionbar><#BF71FF>⬆ <green><outgoing-traffic>  <aqua><outgoing-pps>  <gray>|  <#6AFFF3>⬇ <green><incoming-traffic>  <aqua><incoming-pps>";
    @ConfigField
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