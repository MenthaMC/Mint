package dev.bacteriawa.mint.config.modules.optimizations;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.logging.LogUtils;
import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;
import gg.pufferfish.pufferfish.simd.SIMDDetection;
import org.slf4j.Logger;

@Configuration(name = "use_simd", type = ConfigCategory.optimisations)
public class SIMDConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    @ConfigField
    public static boolean enabled = true;

    public static void loaded(CommentedFileConfig config) {
        if (!enabled){
            return;
        }

        // Attempt to detect vectorization
        try {
            SIMDDetection.isEnabled = SIMDDetection.canEnable(LOGGER);
        } catch (NoClassDefFoundError | Exception ignored) {
            ignored.printStackTrace();
        }

        if (SIMDDetection.isEnabled) {
            LOGGER.info("SIMD operations detected as functional. Will replace some operations with faster versions.");
        } else {
            LOGGER.warn("SIMD operations are available for your server, but are not configured!");
            LOGGER.warn("To enable additional optimizations, add \"--add-modules=jdk.incubator.vector\" to your startup flags, BEFORE the \"-jar\".");
            LOGGER.warn("If you have already added this flag, then SIMD operations are not supported on your JVM or CPU.");
            LOGGER.warn("Debug: Java: {}, test run: {}", System.getProperty("java.version"), SIMDDetection.testRun);
        }
    }
}
