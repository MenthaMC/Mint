package dev.bacteriawa.mint.config.modules.misc;

import abomination.LinearRegionFile;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;
import dev.bacteriawa.mint.utils.EnumRegionFormat;
import net.minecraft.server.MinecraftServer;

@Configuration(name = "region_format", type = ConfigCategory.misc)
public class RegionFormatConfig {
    @ConfigField(comment = "format", commentZh = "区域文件格式")
    public static String format = "MCA";
    @ConfigField(comment = "linear_compression_level", commentZh = "线性压缩级别")
    public static int linearCompressionLevel = 1;
    @ConfigField(comment = "linear_io_thread_count", commentZh = "线性IO线程数")
    public static int linearIoThreadCount = 6;
    @ConfigField(comment = "linear_io_flush_delay_ms", commentZh = "线性IO刷新延迟(毫秒)")
    public static int linearIoFlushDelayMs = 100;
    @ConfigField(comment = "linear_use_virtual_thread", commentZh = "线性模式使用虚拟线程")
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
