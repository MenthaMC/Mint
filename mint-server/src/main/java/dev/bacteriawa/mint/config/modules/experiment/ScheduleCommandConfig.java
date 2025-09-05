package dev.bacteriawa.mint.config.modules.experiment;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "enable_schedule_command", type = ConfigCategory.experiment)
public class ScheduleCommandConfig {
    @ConfigField(comment = "Enable scoreboard command support", commentZh = "启用schedule指令支持")
    public static boolean enabled = false;
}
