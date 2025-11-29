package dev.bacteriawa.mint.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;
import static dev.bacteriawa.mint.config.serialization.SerializerFactory.*;
import dev.bacteriawa.mint.exception.MintRuntimeException;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;

public class ValueConfiguration extends BaseConfiguration {
    private Configurations currentConfigurations;

    protected ValueConfiguration(Path path) {
        super(path);
    }

    @Override
    public void loadOnlyClass(Class<?> clazz) {
        this.currentConfigurations = clazz.getAnnotation(Configurations.class);

        if (!this.currentConfigurations.deprecated()) {
            this.loadOnlyNormalClass(clazz);
            return;
        }

        this.loadOnlyDeprecatedClass(clazz);
    }

    private void loadOnlyNormalClass(Class<?> clazz) {
        Arrays.stream(clazz.getDeclaredFields()).filter(
                field -> field.isAnnotationPresent(Configuration.class)
        ).forEach(this::loadOnlyNormalField);
    }

    private void loadOnlyNormalField(Field field) {
        try {
            Configuration annotation = field.getAnnotation(Configuration.class);
            String[] strings = {
                    this.currentConfigurations.type().name(),
                    this.currentConfigurations.name(),
                    annotation.alisa().isBlank() ? field.getName() : annotation.alisa()
            };
            String fullPath = String.join(".", strings);

            if (annotation.deprecated() || field.isAnnotationPresent(Deprecated.class)) {
                if (!getConfiguration().contains(fullPath))
                    return;

                field.set(null, deserialize(field, getConfiguration().get(fullPath)));
            }

            if (getConfiguration().contains(fullPath)) {
                field.set(null, deserialize(field, getConfiguration().get(fullPath)));
                return;
            }

            getConfiguration().add(fullPath, serialize(field, field.get(null)));
        } catch (IllegalAccessException e) {
            throw new MintRuntimeException(e);
        }
    }

    private void loadOnlyDeprecatedClass(Class<?> clazz) {
        Arrays.stream(clazz.getDeclaredFields()).filter(
                field -> field.isAnnotationPresent(Configuration.class)
        ).forEach(this::loadOnlyDeprecatedClass);
    }

    private void loadOnlyDeprecatedClass(Field field) {
        try {
            Configuration annotation = field.getAnnotation(Configuration.class);
            String[] strings = {
                    this.currentConfigurations.type().name(),
                    this.currentConfigurations.name(),
                    annotation.alisa().isBlank() ? field.getName() : annotation.alisa()
            };
            String fullPath = String.join(".", strings);

            if (!getConfiguration().contains(fullPath)) {
                return;
            }

            field.set(null, serialize(field, getConfiguration().get(fullPath)));
        } catch (IllegalAccessException e) {
            throw new MintRuntimeException(e);
        }
    }
}
