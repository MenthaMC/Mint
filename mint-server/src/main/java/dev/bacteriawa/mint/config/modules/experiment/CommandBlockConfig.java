package dev.bacteriawa.mint.config.modules.experiment;

import static dev.bacteriawa.mint.config.ConfigurationType.experiment;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

@Configurations(name = "force_enable_command_block_execution", type = experiment)
public class CommandBlockConfig {
    @Configuration
    public static boolean enabled = false;
}
