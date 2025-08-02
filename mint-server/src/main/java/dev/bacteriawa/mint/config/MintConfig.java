package dev.bacteriawa.mint.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.commands.MintCommand;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MintConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(MintConfig.class);
    private static final File baseConfigFolder = new File("mint");
    private static final File baseConfigFile = new File(baseConfigFolder, "mint_global.toml");
    private static final CommentedFileConfig configuration;
    private static final Set<Class<?>> moduleClasses = new HashSet<>();

    static {
        if (!baseConfigFolder.exists()) {
            if (!baseConfigFolder.mkdirs()) {
                throw new RuntimeException("Unable to create `mint` folder.");
            }
        }
        configuration = CommentedFileConfig.builder(baseConfigFile)
                .concurrent().charset(StandardCharsets.UTF_8).build();
    }

    public static void setup() {
        Bukkit.getCommandMap().register("mint", new MintCommand());
        moduleClasses.forEach(MintConfig::loaded);
    }

    public static void loadConfig() {
        // Loaded this config file.
        if (baseConfigFile.exists()) {
            configuration.load();
        }

        Set<Class<?>> moduleClasses = getClassesByPackage();
        for (Class<?> clazz : moduleClasses) {
            loadConfigInstance(clazz);
        }

        configuration.save();
    }

    private static void loadConfigInstance(Class<?> moduleClass) {
        try {
            int clazzModifiers = moduleClass.getModifiers();
            if (!moduleClass.isAnnotationPresent(Configuration.class) || moduleClass.isAnnotationPresent(ConfigSkipLoad.class))
                return;
            if (!(Modifier.isPublic(clazzModifiers) && isPlainClass(moduleClass))) {
                LOGGER.error("`{}` must be public and plain class!", moduleClass.getName(), new RuntimeException());
                return;
            }

            Configuration cfg = moduleClass.getDeclaredAnnotation(Configuration.class);
            for (Field field : moduleClass.getDeclaredFields()) {
                int fieldModifiers = field.getModifiers();
                if (!field.isAnnotationPresent(ConfigField.class) || field.isAnnotationPresent(ConfigSkipLoad.class))
                    continue;
                if (!(Modifier.isStatic(fieldModifiers) && Modifier.isPublic(fieldModifiers))) {
                    LOGGER.error("`{}` must be public and static!", field.getName(), new RuntimeException());
                    continue;
                }

                ConfigField config = field.getDeclaredAnnotation(ConfigField.class);
                String fullPath = cfg.type().name() + "." + cfg.name() + "." + field.getName();

                if (!configuration.contains(fullPath)) {
                    configuration.add(fullPath, field.get(null));
                }

                setComment(config.comment(), fullPath);

                field.set(null, configuration.get(fullPath));
            }

            setComment(cfg.comment(), cfg.type().name() + "." + cfg.name());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        moduleClasses.add(moduleClass);
    }

    private static void loaded(Class<?> clazz) {
        try {
            Method loadedMethod = clazz.getDeclaredMethod("loaded", CommentedFileConfig.class);
            int modifiers = loadedMethod.getModifiers();
            if (!(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)))
                return;
            loadedMethod.invoke(null, configuration);
        } catch (NoSuchMethodException ignored) {
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reloadConfig() {
        loadConfig();
    }

    private static void setComment(String[] comment, String fullPath) {
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

    private static Set<Class<?>> getClassesByPackage() {
        try(ScanResult result = new ClassGraph().acceptPackages("dev.bacteriawa.mint.config.modules").scan()) {
            return result.getAllClasses().stream().map(ClassInfo::loadClass).collect(Collectors.toSet());
        }
    }

    private static boolean isPlainClass(Class<?> clazz) {
        return !clazz.isAnnotation() && !clazz.isInterface() && !clazz.isEnum() && !Modifier.isAbstract(clazz.getModifiers());
    }
}

