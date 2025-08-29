package dev.bacteriawa.mint.config.modules.experiment;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "force_enable_command_block_execution", type = ConfigCategory.experiment)
public class CommandBlockConfig {
    @ConfigField(comment = "Force enable command block execution", commentZh = "强制启用命令方块执行")
    public static boolean enabled = false;
}
