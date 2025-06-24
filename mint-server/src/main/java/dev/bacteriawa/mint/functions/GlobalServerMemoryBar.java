package dev.bacteriawa.mint.functions;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import dev.bacteriawa.mint.config.modules.misc.MembarConfig;
import dev.bacteriawa.mint.utils.NullPlugin;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.*;

public class GlobalServerMemoryBar {
    protected static final NullPlugin NULL_PLUGIN = new NullPlugin();
    protected static final Map<UUID, BossBar> uuid2Bossbars = Maps.newConcurrentMap();
    protected static final Map<UUID, ScheduledTask> scheduledTasks = new HashMap<>();
    protected static volatile ScheduledTask scannerTask = null;
    private static final Logger logger = LogUtils.getLogger();

    public static void init(){
        cancelBarUpdateTask();

        scannerTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(NULL_PLUGIN, unused -> {
            try {
                update();
            }catch (Exception e){
                logger.error(e.getLocalizedMessage());
            }
        }, 1, MembarConfig.updateInterval);
    }


    public static void cancelBarUpdateTask(){
        if (scannerTask == null || scannerTask.isCancelled()){
            return;
        }

        scannerTask.cancel();

        for (ScheduledTask task : scheduledTasks.values()) {
            if (!task.isCancelled()) {
                task.cancel();
            }
        }
    }

    public static boolean isPlayerVisible(Player player){
        return ((CraftPlayer) player).getHandle().isMemBarVisible;
    }

    public static void setVisibilityForPlayer(Player target,boolean canSee){
        ((CraftPlayer) target).getHandle().isMemBarVisible = canSee;
    }

    private static void update(){
        doUpdate();
        cleanUp();
    }

    private static void cleanUp(){
        final List<UUID> toCleanUp = new ArrayList<>();

        for (Map.Entry<UUID, ScheduledTask> toCheck : scheduledTasks.entrySet()) {
            if (toCheck.getValue().isCancelled()) {
                toCleanUp.add(toCheck.getKey());
            }
        }

        for (UUID uuid : toCleanUp) {
            scheduledTasks.remove(uuid);
        }
    }

    private static void doUpdate(){
        for (Player player : Bukkit.getOnlinePlayers()) {
            scheduledTasks.computeIfAbsent(player.getUniqueId(), unused -> createBossBarForPlayer(player));
        }
    }

    private static ScheduledTask createBossBarForPlayer(Player apiPlayer) {
        return apiPlayer.getScheduler().runAtFixedRate(NULL_PLUGIN, (unused) -> {
            final UUID playerUUID = apiPlayer.getUniqueId();

            if (!isPlayerVisible(apiPlayer)) {
                final BossBar removed = uuid2Bossbars.remove(playerUUID);

                if (removed != null) {
                    apiPlayer.hideBossBar(removed);
                }

                return;
            }

            MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();

            long used = heap.getUsed();
            long xmx = heap.getMax();

            BossBar targetBossbar = uuid2Bossbars.computeIfAbsent(
                    playerUUID,
                    (unused1) -> BossBar.bossBar(Component.text(""),0.0F, BossBar.Color.valueOf(MembarConfig.memColors.get(3)), BossBar.Overlay.NOTCHED_20)
            );

            apiPlayer.showBossBar(targetBossbar);

            updateMembar(targetBossbar, used, xmx);
        }, () -> {
            final BossBar removed = uuid2Bossbars.remove(apiPlayer.getUniqueId());

            if (removed != null) {
                apiPlayer.hideBossBar(removed);
            }
        }, 1, MembarConfig.updateInterval);
    }

    private static void updateMembar(@NotNull BossBar bar, long used, long xmx){
        double percent = Math.max(Math.min((float) used / xmx, 1.0F), 0.0F);
        bar.name(MiniMessage.miniMessage().deserialize(
                MembarConfig.memBarFormat,
                Placeholder.component("used", getMemoryComponent(used,xmx)),
                Placeholder.component("available",getMaxMemComponent(xmx))
        ));
        bar.color(barColorFromMemory(percent));
        bar.progress((float) percent);
    }

    private static @NotNull Component getMaxMemComponent(double max){
        final BossBar.Color colorBukkit = BossBar.Color.GREEN;
        final String colorString = colorBukkit.name();

        final String content = "<%s><text></%s>";
        final String replaced = String.format(content,colorString,colorString);

        return MiniMessage.miniMessage().deserialize(replaced,Placeholder.parsed("text", String.format("%.2f", max / (1024 * 1024))));
    }

    private static @NotNull Component getMemoryComponent(long used,long max){
        final BossBar.Color colorBukkit = barColorFromMemory(Math.max(Math.min((float) used / max, 1.0F), 0.0F));
        final String colorString = colorBukkit.name();

        final String content = "<%s><text></%s>";
        final String replaced = String.format(content,colorString,colorString);

        return MiniMessage.miniMessage().deserialize(replaced,Placeholder.parsed("text", String.format("%.2f", (double)used / (1024 * 1024))));
    }

    private static BossBar.Color barColorFromMemory(double memPercent){
        if (memPercent == -1){
            return BossBar.Color.valueOf(MembarConfig.memColors.get(3));
        }

        if (memPercent <= 50){
            return BossBar.Color.valueOf(MembarConfig.memColors.getFirst());
        }

        if (memPercent <= 70){
            return BossBar.Color.valueOf(MembarConfig.memColors.get(1));
        }

        return BossBar.Color.valueOf(MembarConfig.memColors.get(2));
    }
}