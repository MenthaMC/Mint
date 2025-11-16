package dev.bacteriawa.mint.config.modules.misc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.commands.TpsBarCommand;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;
import dev.bacteriawa.mint.functions.GlobalServerTpsBar;
import org.bukkit.Bukkit;

import java.util.List;

@Configurations(name = "tpsbar", type = ConfigurationType.misc)
public class TpsBarConfig {
    @Configuration
    public static boolean tpsbarEnabled = true;
    @Configuration
    public static String tpsBarFormat = "<gray>TPS<yellow>:</yellow> <tps> MSPT<yellow>:</yellow> <mspt> Ping<yellow>:</yellow> <ping>ms ChunkHot<yellow>:</yellow> <chunkhot>";
    @Configuration
    public static List<String> tpsColors = List.of("GREEN","YELLOW","RED","PURPLE");
    @Configuration
    public static List<String> pingColors = List.of("GREEN","YELLOW","RED","PURPLE");
    @Configuration
    public static List<String> chunkHotColors = List.of("GREEN","YELLOW","RED","PURPLE");
    @Configuration
    public static int updateInterval = 15;

    public static void loaded(CommentedFileConfig config) {
        if (tpsbarEnabled){
            GlobalServerTpsBar.init();
            Bukkit.getCommandMap().register("tpsbar","mint",new TpsBarCommand());
        }else{
            GlobalServerTpsBar.cancelBarUpdateTask();
        }
    }

}