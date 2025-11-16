package dev.bacteriawa.mint.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringJoiner;

import static dev.bacteriawa.mint.utils.NightConfigUtility.*;

/**
 * @author Frish2021
 * Mint Configuration Parent Class.
 */
public abstract class BaseConfiguration {
    private final CommentedFileConfig config;

    public BaseConfiguration(Path path) {
        this(utf8NightConfig(path));
        if (Files.exists(path)) this.config.load();
    }

    protected BaseConfiguration(CommentedFileConfig config) {
        this.config = config;
    }

    CommentedFileConfig getConfiguration() {
        return config;
    }

    public abstract void loadOnlyClass(Class<?> clazz) throws Exception;
}
