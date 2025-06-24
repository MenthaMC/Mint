package dev.bacteriawa.mint.utils;

import abomination.IRegionFile;

import java.io.IOException;

public interface IRegionCreateFunction {
    IRegionFile create(RegionCreatorInfo info) throws IOException;
}
