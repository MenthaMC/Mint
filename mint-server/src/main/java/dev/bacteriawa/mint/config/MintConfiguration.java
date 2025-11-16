package dev.bacteriawa.mint.config;

import dev.bacteriawa.mint.config.annotation.Configurations;
import dev.bacteriawa.mint.exception.MintRuntimeException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MintConfiguration {
    private static final Queue<Class<?>> loadQueue = new ConcurrentLinkedQueue<>();
    private static final Path mintConfigFolder = Path.of("mint");
    private static final Path mintConfigFile = mintConfigFolder.resolve("mint_global.toml");

    private static final BaseConfiguration cmt = new CMTConfiguration(mintConfigFile);
    private static final BaseConfiguration loader = new LoadConfiguration(cmt.getConfiguration());
    private static final BaseConfiguration value = new ValueConfiguration(cmt.getConfiguration());

    static {
        if (!Files.exists(mintConfigFolder)) {
            try {
                Files.createDirectories(mintConfigFolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void loadAllConfigs() {
        try (ScanResult scanResult = new ClassGraph().enableAnnotationInfo().scan()) {
            scanResult.getClassesWithAnnotation(Configurations.class).forEach(clazz -> {
                Class<?> configClass = clazz.loadClass();

                try {
                    value.loadOnlyClass(configClass);
                    cmt.loadOnlyClass(configClass);
                } catch (Exception e) {
                    throw new MintRuntimeException(e);
                }

                loadQueue.add(configClass);
            });
        }

        cmt.getConfiguration().save(); /* save config file */
    }

    public static void setupAllConfigs() throws Exception {
        while (!loadQueue.isEmpty()) {
            Class<?> clazz = loadQueue.poll();
            loader.loadOnlyClass(clazz);
        }
    }
}
