package dev.bacteriawa.mint.config.modules.misc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.commands.MemoryBarCommand;
import dev.bacteriawa.mint.functions.GlobalServerMemoryBar;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;
import org.bukkit.Bukkit;

import java.util.List;

@Config(name = "membar", category = ConfigCategory.misc)
public class MembarConfig {
    @ConfigField
    public static boolean memoryBarEnabled = true;
    @ConfigField
    public static String memBarFormat = "<gray>Memory usage <yellow>:</yellow> <used>MB<yellow>/</yellow><available>MB";
    @ConfigField
    public static List<String> memColors = List.of("GREEN","YELLOW","RED","PURPLE");
    @ConfigField
    public static int updateInterval = 15;

    public static void loaded(CommentedFileConfig config){
        if (memoryBarEnabled){
            GlobalServerMemoryBar.init();
            Bukkit.getCommandMap().register("membar","mint",new MemoryBarCommand());
        }else{
            GlobalServerMemoryBar.cancelBarUpdateTask();
        }
    }

}