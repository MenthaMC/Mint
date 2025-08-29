package dev.bacteriawa.mint.config.modules.misc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;
import org.apache.logging.log4j.Level;

@Configuration(name = "sentry", type = ConfigCategory.misc)
public class SentryConfig {

    @ConfigField(comment = {
            "Sentry DSN for improved error logging, leave blank to disable,",
            "Obtain from https://sentry.io/"
    }, commentZh = {
            "用于改进错误日志记录的 Sentry DSN，留空则禁用",
            "从 https://sentry.io/ 获取"
    })
    public static String sentryDsn = "";

    @ConfigField(comment = "Logs with a level higher than or equal to this level will be recorded.",
                 commentZh = "将记录等级高于或等于此级别的日志")
    public static String logLevel = "WARN";

    @ConfigField(comment = "Only log with a Throwable will be recorded after enabling this.",
                 commentZh = "启用后仅记录带有异常的日志")
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
