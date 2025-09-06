package me.coderfrish.mint.tick;

import dev.bacteriawa.mint.utils.NullPlugin;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;

public class EmulationTick implements Runnable {
    private static final NullPlugin plugin = new NullPlugin();
    private static final int TICK_MS = 50; // 50ms = 20TPS

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();
        while (MinecraftServer.getServer().isRunning()) {
            long current = System.currentTimeMillis();
            if (current - lastTime >= TICK_MS) {
                MinecraftServer.getServer().getFunctions().tick();
                lastTime = current;
            }
        }
    }

    public static void startTick() {
        Bukkit.getAsyncScheduler().runNow(plugin, (t) -> {
            new EmulationTick().run();
        });
    }
}
