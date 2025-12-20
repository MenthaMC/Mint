package dev.bacteriawa.mint.functions;

import ca.spottedleaf.moonrise.common.time.TickData;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import io.papermc.paper.threadedregions.*;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import dev.bacteriawa.mint.config.modules.misc.RegionBarConfig;
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

import java.text.DecimalFormat;
import java.util.*;

public class GlobalServerRegionBar {
    protected static final NullPlugin NULL_PLUGIN = new NullPlugin();
    protected static final Map<UUID, BossBar> uuid2Bossbars = Maps.newConcurrentMap();
    protected static final Map<UUID, ScheduledTask> scheduledTasks = new HashMap<>();

    protected static volatile ScheduledTask scannerTask = null;
    private static final Logger logger = LogUtils.getLogger();

    private static final ThreadLocal<DecimalFormat> ONE_DECIMAL_PLACES = ThreadLocal.withInitial(() -> new DecimalFormat("#,##0.0"));

    public static void init() {
        cancelBarUpdateTask();

        scannerTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(NULL_PLUGIN, unused -> {
            try {
                update();
                cleanUp();
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
            }
        }, 1, RegionBarConfig.updateInterval);
    }

    public static void cancelBarUpdateTask() {
        if (scannerTask == null || scannerTask.isCancelled()) {
            return;
        }

        scannerTask.cancel();

        for (ScheduledTask task : scheduledTasks.values()) {
            if (!task.isCancelled()) {
                task.cancel();
            }
        }
    }

    public static boolean isPlayerVisible(Player player) {
        return ((CraftPlayer) player).getHandle().isRegionBarVisible;
    }

    public static void setVisibilityForPlayer(Player target, boolean canSee) {
        ((CraftPlayer) target).getHandle().isRegionBarVisible = canSee;
    }

    private static void update() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            scheduledTasks.computeIfAbsent(player.getUniqueId(), unused -> createBossBarForPlayer(player));
        }
    }

    private static void cleanUp() {
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

    public static ScheduledTask createBossBarForPlayer(@NotNull Player apiPlayer) {
        final UUID playerUUID = apiPlayer.getUniqueId();

        return apiPlayer.getScheduler().runAtFixedRate(NULL_PLUGIN, (n) -> {
            if (!isPlayerVisible(apiPlayer)) {
                final BossBar removed = uuid2Bossbars.remove(playerUUID);

                if (removed != null) {
                    apiPlayer.hideBossBar(removed);
                }
                return;
            }

            final ThreadedRegionizer.ThreadedRegion<TickRegions.TickRegionData, TickRegions.TickRegionSectionData> region = TickRegionScheduler.getCurrentRegion();
            final TickData.TickReportData reportData = region.getData().getRegionSchedulingHandle().getTickReport5s(System.nanoTime());
            final TickRegions.RegionStats regionStats = region.getData().getRegionStats();

            BossBar targetBossbar = uuid2Bossbars.computeIfAbsent(
                    playerUUID,
                    unused -> BossBar.bossBar(Component.text(""), 0.0F, BossBar.Color.GREEN, BossBar.Overlay.NOTCHED_20)
            );

            apiPlayer.showBossBar(targetBossbar);

            if (reportData != null) {
                final double utilisation = reportData.utilisation();
                final int chunkCount = regionStats.getChunkCount();
                final int playerCount = regionStats.getPlayerCount();
                final int entityCount = regionStats.getEntityCount();

                updateRegionBar(utilisation, chunkCount, playerCount, entityCount, targetBossbar);
            }
        }, () -> {
            final BossBar removed = uuid2Bossbars.remove(playerUUID); // Auto clean up it

            if (removed != null) {
                apiPlayer.hideBossBar(removed);
            }
        }, 1, RegionBarConfig.updateInterval);
    }

    private static void updateRegionBar(double utilisation, int chunks, int players, int entities, @NotNull BossBar bar) {
        final double utilisationPercent = utilisation * 100.0;
        final String formattedUtil = ONE_DECIMAL_PLACES.get().format(utilisationPercent);

        bar.name(MiniMessage.miniMessage().deserialize(
                RegionBarConfig.regionBarFormat,
                Placeholder.component("util", getUtilComponent(formattedUtil)),
                Placeholder.component("chunks", getChunksComponent(chunks)),
                Placeholder.component("players", getPlayersComponent(players)),
                Placeholder.component("entities", getEntitiesComponent(entities))
        ));

        bar.color(barColorFromUtil(utilisationPercent));
        bar.progress((float) Math.min(1.0, Math.max(utilisation, 0)));
    }

    private static @NotNull Component getEntitiesComponent(int entities) {
        final String content = "<text>";
        return MiniMessage.miniMessage().deserialize(content, Placeholder.parsed("text", String.valueOf(entities)));
    }

    private static @NotNull Component getPlayersComponent(int players) {
        final String content = "<text>";
        return MiniMessage.miniMessage().deserialize(content, Placeholder.parsed("text", String.valueOf(players)));
    }

    private static @NotNull Component getChunksComponent(int chunks) {
        final String content = "<text>";
        return MiniMessage.miniMessage().deserialize(content, Placeholder.parsed("text", String.valueOf(chunks)));
    }

    private static @NotNull Component getUtilComponent(String formattedUtil) {
        final BossBar.Color colorBukkit = barColorFromUtil(Double.parseDouble(formattedUtil));
        final String colorString = colorBukkit.name();

        final String content = "<%s><text></%s>";
        final String replaced = String.format(content, colorString, colorString);

        return MiniMessage.miniMessage().deserialize(replaced, Placeholder.parsed("text", formattedUtil + "%"));
    }

    private static BossBar.Color barColorFromUtil(double util) {
        if (util >= 100) {
            return BossBar.Color.valueOf(RegionBarConfig.utilColors.get(3)); // PURPLE
        }

        if (util >= 70) {
            return BossBar.Color.valueOf(RegionBarConfig.utilColors.get(2)); // RED
        }

        if (util >= 50) {
            return BossBar.Color.valueOf(RegionBarConfig.utilColors.get(1)); // YELLOW
        }

        return BossBar.Color.valueOf(RegionBarConfig.utilColors.get(0)); // GREEN
    }
}