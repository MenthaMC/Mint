package dev.bacteriawa.mint.config.modules.misc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.commands.MemoryBarCommand;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;
import dev.bacteriawa.mint.functions.GlobalServerMemoryBar;
import org.bukkit.Bukkit;

import java.util.List;

@Configurations(name = "membar", type = ConfigurationType.misc)
public class MembarConfig {
    @Configuration
    public static boolean memoryBarEnabled = true;
    @Configuration
    public static String memBarFormat = "<gray>Memory usage <yellow>:</yellow> <used>MB<yellow>/</yellow><available>MB";
    @Configuration
    public static List<String> memColors = List.of("GREEN","YELLOW","RED","PURPLE");
    @Configuration
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