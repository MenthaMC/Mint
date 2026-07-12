package dev.bacteriawa.mint.utils;

import abomination.LinearRegionFile;
import dev.bacteriawa.mint.config.modules.misc.RegionFormatConfig;
import net.minecraft.world.level.chunk.storage.RegionFile;
import org.jetbrains.annotations.Nullable;

public enum EnumRegionFormat {
    MCA("mca", "mca" , (info) -> new RegionFile(info.info(), info.filePath(), info.folder(), info.sync())),
    LINEAR_V2("linear_v2", "linear" ,(info) -> new LinearRegionFile(info.info(), info.filePath(), info.folder(), info.sync(), RegionFormatConfig.linearCompressionLevel)),
    B_LINEAR("b_linear", "b_linear", (info) -> new me.earthme.luminol.data.BufferedLinearRegionFile(info.filePath(), RegionFormatConfig.linearCompressionLevel, RegionFormatConfig.blinearFlusher));

    private final String name;
    private final String argument;
    private final IRegionCreateFunction creator;

    EnumRegionFormat(String name, String argument, IRegionCreateFunction creator) {
        this.name = name;
        this.argument = argument;
        this.creator = creator;
    }

    @Nullable
    public static EnumRegionFormat fromString(String string) {
        for (EnumRegionFormat format : values()) {
            if (format.name.equalsIgnoreCase(string)) {
                return format;
            }
        }

        return null;
    }

    public IRegionCreateFunction getCreator() {
        return this.creator;
    }

    public String getArgument() {
        return this.argument;
    }
}