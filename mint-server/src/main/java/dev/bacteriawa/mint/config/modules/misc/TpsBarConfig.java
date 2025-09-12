package dev.bacteriawa.mint.config.modules.misc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.commands.TpsBarCommand;
import dev.bacteriawa.mint.functions.GlobalServerTpsBar;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;
import org.bukkit.Bukkit;

import java.util.List;

@Config(name = "tpsbar", category = ConfigCategory.misc)
public class TpsBarConfig {
    @ConfigField
    public static boolean tpsbarEnabled = true;
    @ConfigField
    public static String tpsBarFormat = "<gray>TPS<yellow>:</yellow> <tps> MSPT<yellow>:</yellow> <mspt> Ping<yellow>:</yellow> <ping>ms ChunkHot<yellow>:</yellow> <chunkhot>";
    @ConfigField
    public static List<String> tpsColors = List.of("GREEN","YELLOW","RED","PURPLE");
    @ConfigField
    public static List<String> pingColors = List.of("GREEN","YELLOW","RED","PURPLE");
    @ConfigField
    public static List<String> chunkHotColors = List.of("GREEN","YELLOW","RED","PURPLE");
    @ConfigField
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