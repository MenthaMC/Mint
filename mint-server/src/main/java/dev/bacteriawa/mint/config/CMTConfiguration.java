package dev.bacteriawa.mint.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.StringJoiner;

import static dev.bacteriawa.mint.language.MintLanguage.translateComments;

public class CMTConfiguration extends BaseConfiguration {
    private Configurations currentConfigurations;

    public CMTConfiguration(CommentedFileConfig config) {
        super(config);
    }

    @Override
    public void loadOnlyClass(Class<?> clazz) {
        this.currentConfigurations = clazz.getAnnotation(Configurations.class);

        String[] strings = {
                currentConfigurations.type().toString(),
                currentConfigurations.name()
        };
        String parentPath = String.join(".", strings);

        if (currentConfigurations.deprecated()) {
            return;
        }

        Arrays.stream(clazz.getDeclaredFields()).filter(
                field -> field.isAnnotationPresent(Configuration.class)
        ).forEach(this::loadOnlyField);

        String[] comments = translateComments(parentPath, currentConfigurations.comments());
        this.addConfigComment(parentPath, comments);
    }

    private void loadOnlyField(Field field) {
        Configuration annotation0 = field.getAnnotation(Configuration.class);
        String[] strings = {
                currentConfigurations.type().toString(),
                currentConfigurations.name(),
                annotation0.alisa().isBlank() ? field.getName() : annotation0.alisa()
        };
        String fullPath = String.join(".", strings);

        if (annotation0.deprecated() || field.isAnnotationPresent(Deprecated.class))
            if (!getConfiguration().contains(fullPath))
                return;

        String[] comments = translateComments(fullPath, currentConfigurations.comments());
        this.addConfigComment(fullPath, comments);
    }

    private void addConfigComment(String fullPath, String comment) {
        getConfiguration().setComment(fullPath, comment);
    }

    private void addConfigComment(String fullPath, String[] comments) {
        if (comments.length > 0) {
            StringJoiner joiner = new StringJoiner("\n");
            for (String line : comments) {
                joiner.add(" " + line);
            }
            addConfigComment(fullPath, joiner.toString());
        }
    }
}
