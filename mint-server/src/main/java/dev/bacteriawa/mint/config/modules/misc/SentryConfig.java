package dev.bacteriawa.mint.config.modules.misc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import me.coderfrish.mint.config.ConfigCategory;
import me.coderfrish.mint.config.annotation.Config;
import me.coderfrish.mint.config.annotation.ConfigField;
import org.apache.logging.log4j.Level;

@Config(name = "sentry", category = ConfigCategory.misc)
public class SentryConfig {

    @ConfigField
    public static String sentryDsn = "";

    @ConfigField
    public static String logLevel = "WARN";

    @ConfigField
    public static boolean onlyLogThrown = true;


    public static void loaded(CommentedFileConfig configInstance) {
        String sentryEnvironment = System.getenv("SENTRY_DSN");

        sentryDsn = sentryEnvironment != null && !sentryEnvironment.isBlank()
                ? sentryEnvironment
                : configInstance.getOrElse("sentry.dsn", sentryDsn);

        logLevel = configInstance.getOrElse("sentry.log-level", logLevel);
        onlyLogThrown = configInstance.getOrElse("sentry.only-log-thrown", onlyLogThrown);

        if (sentryDsn != null && !sentryDsn.isBlank()) {
            gg.pufferfish.pufferfish.sentry.SentryManager.init(Level.getLevel(logLevel));
        }
    }
}
