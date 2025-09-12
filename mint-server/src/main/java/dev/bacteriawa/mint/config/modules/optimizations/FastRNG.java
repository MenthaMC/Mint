package dev.bacteriawa.mint.config.modules.optimizations;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.logging.LogUtils;
import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;
import org.slf4j.Logger;

import java.util.random.RandomGeneratorFactory;

@Config(name = "faster_random_generator", category = ConfigCategory.optimisations)
public class FastRNG {
    private static final Logger LOGGER = LogUtils.getLogger();
    @ConfigField
    public static boolean enabled = false;

    @ConfigField
    public static boolean enableForWorldgen = false;

    @ConfigField
    public static String randomGenerator = "Xoroshiro128PlusPlus";

    @ConfigField
    public static boolean warnForSlimeChunk = true;

    @ConfigField
    public static boolean useLegacyForSlimeChunk = false;

    @ConfigField
    public static boolean useDirectImpl = false;

    public static boolean worldgenEnabled() {
        return enabled && enableForWorldgen;
    } // Helper function

    public static void loaded(CommentedFileConfig configInstance) {

        if (enabled) {
            try {
                RandomGeneratorFactory.of(randomGenerator);
            } catch (Exception e) {
                LOGGER.error("Faster random generator is enabled but {} is not supported by your JVM, " +
                        "falling back to legacy random source.", randomGenerator);
                enabled = false;
            }
        }

        if (enabled && warnForSlimeChunk) {
            LOGGER.warn("You enabled faster random generator, it will offset location of slime chunk");
            LOGGER.warn("If your server has slime farms or facilities need vanilla slime chunk,");
            LOGGER.warn("set performance.faster-random-generator.use-legacy-random-for-slime-chunk " +
                    "to true to use LegacyRandomSource for slime chunk generation.");
            LOGGER.warn("Set performance.faster-random-generator.warn-for-slime-chunk to false to " +
                    "disable this warning.");
        }
    }
}
