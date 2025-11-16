package dev.bacteriawa.mint.config.modules.misc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;
import org.apache.logging.log4j.Level;

@Configurations(name = "sentry", type = ConfigurationType.misc)
public class SentryConfig {

    @Configuration
    public static String sentryDsn = "";

    @Configuration
    public static String logLevel = "WARN";

    @Configuration
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
