package dev.bacteriawa.mint.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import java.lang.reflect.Method;

public class LoadConfiguration extends BaseConfiguration {
    protected LoadConfiguration(CommentedFileConfig config) {
        super(config);
    }

    @Override
    public void loadOnlyClass(Class<?> clazz) throws Exception {
        try {
            Method loaded = clazz.getDeclaredMethod("loaded", CommentedFileConfig.class);
            loaded.invoke(null, getConfiguration());
        } catch (NoSuchMethodException ignore) {
        }
    }
}
