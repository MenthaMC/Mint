package dev.bacteriawa.mint.config.modules.misc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;
import dev.bacteriawa.mint.commands.TpsBarCommand;
import dev.bacteriawa.mint.functions.GlobalServerTpsBar;
import org.bukkit.Bukkit;

import java.util.List;

@Configuration(name = "tpsbar", type = ConfigCategory.misc)
public class TpsBarConfig {
    @ConfigField(comment = "enabled", commentZh = "启用TPS显示条")
    public static boolean tpsbarEnabled = true;
    @ConfigField(comment = "format", commentZh = "显示格式")
    public static String tpsBarFormat = "<gray>TPS<yellow>:</yellow> <tps> MSPT<yellow>:</yellow> <mspt> Ping<yellow>:</yellow> <ping>ms ChunkHot<yellow>:</yellow> <chunkhot>";
    @ConfigField(comment = "tps_color_list", commentZh = "TPS颜色列表")
    public static List<String> tpsColors = List.of("GREEN","YELLOW","RED","PURPLE");
    @ConfigField(comment = "ping_color_list", commentZh = "延迟颜色列表")
    public static List<String> pingColors = List.of("GREEN","YELLOW","RED","PURPLE");
    @ConfigField(comment = "chunkhot_color_list", commentZh = "区块热点颜色列表")
    public static List<String> chunkHotColors = List.of("GREEN","YELLOW","RED","PURPLE");
    @ConfigField(comment = "update_interval_ticks", commentZh = "更新间隔(刻)")
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