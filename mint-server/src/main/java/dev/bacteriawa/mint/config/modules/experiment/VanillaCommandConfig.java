package dev.bacteriawa.mint.config.modules.experiment;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.bacteriawa.mint.config.ConfigurationType.experiment;

@Configurations(type = experiment, name = "force_enable_vanilla_command")
public class VanillaCommandConfig {
    @Configuration
    public static boolean schedule = false;

    @Configuration
    public static boolean scoreboard = false;

    @Configuration
    public static boolean function = false;

    @Configuration
    public static boolean datapack = false;

    @Configuration
    public static boolean item = false;

    @Configuration
    public static boolean tag = false;

    public static void loaded(CommentedFileConfig config) {
        Logger logger = LoggerFactory.getLogger(VanillaCommandConfig.class);

        if (function) {
            if (dev.bacteriawa.mint.config.modules.globals.WarnLoggerConfig.vanillaCommandWarn)
                logger.warn("It has been detected that you have forcibly enabled the /function command, which is an experimental feature, please use it with caution.");
        }

        if (datapack) {
            if (dev.bacteriawa.mint.config.modules.globals.WarnLoggerConfig.vanillaCommandWarn)
                logger.warn("It has been detected that you have forcibly enabled the /datapack command, which is an experimental feature, please use it with caution.");
        }

        if (schedule) {
            if (dev.bacteriawa.mint.config.modules.globals.WarnLoggerConfig.vanillaCommandWarn)
                logger.warn("It has been detected that you have forcibly enabled the /schedule command, which is an experimental feature, please use it with caution.");
        }

        if (scoreboard) {
            if (dev.bacteriawa.mint.config.modules.globals.WarnLoggerConfig.vanillaCommandWarn)
                logger.warn("It has been detected that you have forcibly enabled the /scoreboard command, which is an experimental feature, please use it with caution.");
        }

        if (item) {
            if (dev.bacteriawa.mint.config.modules.globals.WarnLoggerConfig.vanillaCommandWarn)
                logger.warn("It has been detected that you have forcibly enabled the /item command, which is an experimental feature, please use it with caution.");
        }

        if (tag) {
            if (dev.bacteriawa.mint.config.modules.globals.WarnLoggerConfig.vanillaCommandWarn)
                logger.warn("It has been detected that you have forcibly enabled the /tag command, which is an experimental feature, please use it with caution.");
        }
    }
}
