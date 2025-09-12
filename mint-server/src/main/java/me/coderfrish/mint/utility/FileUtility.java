package me.coderfrish.mint.utility;

import dev.bacteriawa.mint.exception.MintRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class FileUtility {
    private static final Logger logger = LoggerFactory.getLogger("FileHelper");

    public static void createDirectory(File directory) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public static void createFile(File file) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            logger.warn("Failed to create file - {}", file.getAbsolutePath());
            throw new MintRuntimeException(e);
        }
    }
}
