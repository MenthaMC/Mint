package dev.bacteriawa.mint.config.modules.experiment;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "enable_datapack_function_command", type = ConfigCategory.experiment)
public class DatapackCommandConfig {
    @ConfigField
    public static boolean enabled = false;

    public static void loaded(CommentedFileConfig config){
        if (enabled){
            me.coderfrish.tick.EmulationTickRunnable.startTick();
        }
    }
}
