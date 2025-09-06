package dev.bacteriawa.mint.config.modules.misc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.commands.NetworkAnalyserCommand;
import me.coderfrish.mint.config.ConfigCategory;
import me.coderfrish.mint.config.annotation.Config;
import me.coderfrish.mint.config.annotation.ConfigField;
import org.bukkit.Bukkit;

@Config(name = "networkanalyser", category = ConfigCategory.misc)
public class NetworkAnalyserConfig {
    @ConfigField
    public static boolean networkAnalyserEnabled = false;

    public static void loaded(CommentedFileConfig config) {
        if (networkAnalyserEnabled) {
            Bukkit.getCommandMap().register("networkanalyser", "mint", new NetworkAnalyserCommand());
        }
    }
}

