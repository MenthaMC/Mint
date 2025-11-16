package dev.bacteriawa.mint.utils;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class NightConfigUtility {
    public static CommentedFileConfig utf8NightConfig(Path path) {
        return CommentedFileConfig.builder(path).charset(StandardCharsets.UTF_8).build();
    }
}
