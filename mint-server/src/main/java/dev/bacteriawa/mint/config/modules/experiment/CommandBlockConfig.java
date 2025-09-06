package dev.bacteriawa.mint.config.modules.experiment;

import me.coderfrish.mint.config.ConfigCategory;
import me.coderfrish.mint.config.annotation.Config;
import me.coderfrish.mint.config.annotation.ConfigField;

@Config(name = "force_enable_command_block_execution", category = ConfigCategory.experiment)
public class CommandBlockConfig {
    @ConfigField
    public static boolean enabled = false;
}
