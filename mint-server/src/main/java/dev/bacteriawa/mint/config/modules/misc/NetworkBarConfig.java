package dev.bacteriawa.mint.config.modules.misc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.commands.NetworkBarCommand;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;
import dev.bacteriawa.mint.functions.GlobalServerNetworkBar;
import org.bukkit.Bukkit;

@Configurations(name = "networkbar", type = ConfigurationType.misc)
public class NetworkBarConfig {
    @Configuration
    public static boolean networkBarEnabled = true;
    @Configuration
    public static String trafficFormat = "<actionbar><#BF71FF>⬆ <green><outgoing-traffic>  <aqua><outgoing-pps>  <gray>|  <#6AFFF3>⬇ <green><incoming-traffic>  <aqua><incoming-pps>";
    @Configuration
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