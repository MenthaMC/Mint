package dev.bacteriawa.mint.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.bacteriawa.mint.commands.MintCommand;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import dev.bacteriawa.mint.config.annotation.ConfigField;
import dev.bacteriawa.mint.config.annotation.ConfigPackage;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.language.MintLanguage;
import org.bukkit.Bukkit;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class MintConfig {
    private static final CommentedFileConfig configuration;
    private static final Set<Class<?>> configurations = new HashSet<>();
    private static final File mintConfigFolder = new File("mint");
    private static final File mintConfigFile = new File(mintConfigFolder, "mint_global.toml");

    static {
        Set<String> packages = new HashSet<>();
        try (ScanResult scanResult = new ClassGraph().enableAnnotationInfo().scan()) {
            scanResult.getPackageInfo().stream().filter(packageInfo -> packageInfo.hasAnnotation(ConfigPackage.class))
                    .forEach(packageInfo -> packages.add(packageInfo.getName()));
        }

        try (ScanResult scanResult = new ClassGraph().acceptPackages(packages.toArray(new String[0])).enableClassInfo().enableAnnotationInfo().scan()) {
            scanResult.getAllClasses().stream().filter(classInfo -> classInfo.hasAnnotation(Config.class))
                    .forEach(classInfo -> configurations.add(classInfo.loadClass()));
        }

        if (!mintConfigFolder.exists()) {
            MintConfig.mintConfigFolder.mkdirs();
        }

        configuration = CommentedFileConfig.builder(mintConfigFile)
                .concurrent().charset(StandardCharsets.UTF_8).build();
    }

    public static void setupAllConfigs() throws InvocationTargetException, IllegalAccessException {
        Bukkit.getPluginManager().addPermission(MintCommand.MINT_USER_PERMISSION);
        Bukkit.getPluginManager().addPermission(MintCommand.MINT_ADMIN_PERMISSION);
        Bukkit.getCommandMap().register("mint", new MintCommand());
        for (Class<?> configClass : configurations) {
            try {
                Method loaded = configClass.getDeclaredMethod("loaded", CommentedFileConfig.class);
                loaded.invoke(null, configuration);
            } catch (NoSuchMethodException ignore) {
            }
        }
    }

    public static void loadAllConfigs() throws IllegalAccessException {
        if (mintConfigFile.exists()) {
            configuration.load();
        }

        for (Class<?> configClass : configurations) {
            loadConfigAsInstance(configClass);
        }

        for (Class<?> configClass : configurations) {
            loadConfigCommentAsInstance(configClass);
        }

        configuration.save();
    }

    private static void loadConfigCommentAsInstance(Class<?> configClass) {
        if (configClass.isAnnotationPresent(Deprecated.class))
            return;

        Config configuration = configClass.getAnnotation(Config.class);
        for (Field field : configClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigField.class)) {
                ConfigField fieldAnnotation = field.getAnnotation(ConfigField.class);
                String fullPath = configuration.category().name() + "." + configuration.name() + "." + field.getName();

                if (field.isAnnotationPresent(Deprecated.class))
                    if (!MintConfig.configuration.contains(fullPath))
                        continue;

                String[] rawComment = fieldAnnotation.comments();
                if (rawComment.length == 0) {
                    if (MintLanguage.getLanguage().has(fullPath)) {
                        JsonElement element = MintLanguage.getLanguage().get(fullPath);
                        if (element instanceof JsonArray array) {
                            rawComment = array.asList().stream().map(JsonElement::getAsString).toList().toArray(new String[0]);
                        } else if (element instanceof JsonPrimitive string) {
                            rawComment = new String[]{string.getAsString()};
                        }
                    }
                }

                addConfigComment(rawComment, fullPath);
            }
        }
    }

    private static void loadConfigAsInstance(Class<?> configClass) throws IllegalAccessException {
        Config configuration = configClass.getAnnotation(Config.class);
        String fullPath = configuration.category().name() + "." + configuration.name();
        if (configClass.isAnnotationPresent(Deprecated.class)) {
            loadDeprecatedConfig(configClass, configuration);
        } else {
            loadNormalConfig(configClass, configuration);
        }

        addConfigComment(configuration.comments(), fullPath);
    }

    private static void loadDeprecatedConfig(Class<?> clazz, Config configuration) throws IllegalAccessException {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigField.class)) {
                String fullPath = configuration.category().name() + "." + configuration.name() + "." + field.getName();

                if (!MintConfig.configuration.contains(fullPath)) {
                    continue;
                }

                field.set(null, MintConfig.configuration.get(fullPath));
            }
        }
    }

    private static void loadNormalConfig(Class<?> clazz, Config configuration) throws IllegalAccessException {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigField.class)) {
                String fullPath = configuration.category().name() + "." + configuration.name() + "." + field.getName();

                if (field.isAnnotationPresent(Deprecated.class)) {
                    if (!MintConfig.configuration.contains(fullPath))
                        continue;

                    field.set(null, MintConfig.configuration.get(fullPath));
                }

                if (MintConfig.configuration.contains(fullPath)) {
                    field.set(null, MintConfig.configuration.get(fullPath));
                    continue;
                }

                MintConfig.configuration.add(fullPath, field.get(null));
            }
        }
    }

    private static void addConfigComment(String[] comment, String fullPath) {
        if (comment.length > 0) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < comment.length; i++) {
                builder.append(" ").append(comment[i]);
                if (i < comment.length - 1)
                    builder.append("\n");
            }

            configuration.setComment(fullPath, builder.toString());
        }
    }
}
