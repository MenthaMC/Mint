package dev.bacteriawa.mint.config.modules.misc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.commands.NetworkAnalyserCommand;
import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;
import org.bukkit.Bukkit;

@Configuration(name = "networkanalyser", type = ConfigCategory.misc)
public class NetworkAnalyserConfig {
    @ConfigField(comment = "enabled", commentZh = "启用网络分析器")
    public static boolean networkAnalyserEnabled = false;

    public static void loaded(CommentedFileConfig config) {
        if (networkAnalyserEnabled) {
            Bukkit.getCommandMap().register("networkanalyser", "mint", new NetworkAnalyserCommand());
        }
    }
}

