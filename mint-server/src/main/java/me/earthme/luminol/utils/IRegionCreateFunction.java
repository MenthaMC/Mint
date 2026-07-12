package me.earthme.luminol.utils;

import abomination.IRegionFile;
import dev.bacteriawa.mint.utils.RegionCreatorInfo;

import java.io.IOException;

@FunctionalInterface
public interface IRegionCreateFunction {
    IRegionFile create(RegionCreatorInfo info) throws IOException;
}