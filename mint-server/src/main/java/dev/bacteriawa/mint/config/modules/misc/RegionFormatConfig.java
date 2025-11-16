package dev.bacteriawa.mint.config.modules.misc;

import abomination.LinearRegionFile;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;
import dev.bacteriawa.mint.utils.EnumRegionFormat;
import net.minecraft.server.MinecraftServer;

@Configurations(name = "region_format", type = ConfigurationType.misc)
public class RegionFormatConfig {
    @Configuration
    public static String format = "MCA";
    @Configuration
    public static int linearCompressionLevel = 1;
    @Configuration
    public static int linearIoThreadCount = 6;
    @Configuration
    public static int linearIoFlushDelayMs = 100;
    @Configuration
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
