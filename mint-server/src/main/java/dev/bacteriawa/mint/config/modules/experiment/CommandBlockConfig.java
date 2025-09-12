package dev.bacteriawa.mint.config.modules.experiment;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;

@Config(name = "force_enable_command_block_execution", category = ConfigCategory.experiment)
public class CommandBlockConfig {
    @ConfigField
    public static boolean enabled = false;
}
