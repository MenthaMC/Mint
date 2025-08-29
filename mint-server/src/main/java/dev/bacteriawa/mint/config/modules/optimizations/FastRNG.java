package dev.bacteriawa.mint.config.modules.optimizations;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.logging.LogUtils;
import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;
import org.slf4j.Logger;

import java.util.random.RandomGeneratorFactory;

@Configuration(name = "faster-random-generator", type = ConfigCategory.optimisations)
public class FastRNG {
    private static final Logger LOGGER = LogUtils.getLogger();
    @ConfigField(comment = {
            "Use faster random generator?",
            "Requires a JVM that supports RandomGenerator.",
            "Some JREs don't support this."
    }, commentZh = {
            "使用更快的随机数生成器？",
            "需要支持 RandomGenerator 的 JVM。",
            "某些 JRE 不支持此功能。"
    })
    public static boolean enabled = false;
    @ConfigField(comment = {
            "Which random generator will be used?",
            "See https://openjdk.org/jeps/356"
    }, commentZh = {
            "将使用哪个随机数生成器？",
            "查看 https://openjdk.org/jeps/356"
    })
    public static boolean enableForWorldgen = false;
    @ConfigField(comment = {
            "Enable faster random generator for world generation.",
            "WARNING: This will affect world generation!!!"
    }, commentZh = {
            "为世界生成启用更快的随机数生成器。",
            "警告：这将影响世界生成！！！"
    })
    public static String randomGenerator = "Xoroshiro128PlusPlus";
    @ConfigField(comment = {
            "Warn if you are not using legacy random source for slime chunk generation."
    }, commentZh = {
            "如果您没有使用传统随机源进行史莱姆区块生成，则警告。"
    })
    public static boolean warnForSlimeChunk = true;
    @ConfigField(comment = {
            "Use legacy random source for slime chunk generation,to follow vanilla behavior."
    }, commentZh = {
            "使用传统随机源进行史莱姆区块生成，以遵循原版行为。"
    })
    public static boolean useLegacyForSlimeChunk = false;
    @ConfigField(comment = {
            "Use direct random implementation instead of delegating to Java's RandomGenerator.",
            "This may improve performance but potentially changes RNG behavior."
    }, commentZh = {
            "使用直接随机实现而不是委托给 Java 的 RandomGenerator。",
            "这可能改善性能，但可能改变 RNG 行为。"
    })
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
