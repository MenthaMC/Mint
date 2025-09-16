package dev.bacteriawa.mint.config.modules.misc;

import abomination.LinearRegionFile;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.utils.EnumRegionFormat;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;
import net.minecraft.server.MinecraftServer;

@Config(name = "region_format", category = ConfigCategory.misc)
public class RegionFormatConfig {
    @ConfigField
    public static String format = "MCA";
    @ConfigField
    public static int linearCompressionLevel = 1;
    @ConfigField
    public static int linearIoThreadCount = 6;
    @ConfigField
    public static int linearIoFlushDelayMs = 100;
    @ConfigField
    public static boolean linearUseVirtualThread = true;

    public static EnumRegionFormat regionFormat;

    public static void loaded(CommentedFileConfig config) {
        regionFormat = EnumRegionFormat.fromString(format.toUpperCase());

        if (regionFormat == null) {
            throw new RuntimeException("Invalid region format: " + format);
        }

        if (regionFormat == EnumRegionFormat.LINEAR_V2) {
            if (RegionFormatConfig.linearCompressionLevel > 23 || RegionFormatConfig.linearCompressionLevel < 1) {
                MinecraftServer.LOGGER.error("Linear region compression level should be between 1 and 22 in config: {}", RegionFormatConfig.linearCompressionLevel);
                MinecraftServer.LOGGER.error("Falling back to compression level 1.");
                RegionFormatConfig.linearCompressionLevel = 1;
            }

            LinearRegionFile.SAVE_DELAY_MS = linearIoFlushDelayMs;
            LinearRegionFile.SAVE_THREAD_MAX_COUNT = linearIoThreadCount;
            LinearRegionFile.USE_VIRTUAL_THREAD = linearUseVirtualThread;
        }
    }
}
