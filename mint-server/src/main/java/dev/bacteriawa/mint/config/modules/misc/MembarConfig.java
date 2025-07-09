package dev.bacteriawa.mint.config.modules.misc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;
import dev.bacteriawa.mint.commands.MemoryBarCommand;
import dev.bacteriawa.mint.functions.GlobalServerMemoryBar;
import org.bukkit.Bukkit;

import java.util.List;

@Configuration(name = "membar", type = ConfigCategory.misc)
public class MembarConfig {
    @ConfigField(comment = "enabled")
    public static boolean memoryBarEnabled = true;
    @ConfigField(comment = "format")
    public static String memBarFormat = "<gray>Memory usage <yellow>:</yellow> <used>MB<yellow>/</yellow><available>MB";
    @ConfigField(comment = "memory_color_list")
    public static List<String> memColors = List.of("GREEN","YELLOW","RED","PURPLE");
    @ConfigField(comment = "update_interval_ticks")
    public static int updateInterval = 15;
}