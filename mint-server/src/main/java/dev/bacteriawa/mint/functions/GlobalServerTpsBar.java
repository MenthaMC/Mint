package dev.bacteriawa.mint.functions;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import io.papermc.paper.threadedregions.ThreadedRegionizer;
import ca.spottedleaf.common.time.TickData;
import io.papermc.paper.threadedregions.TickRegionScheduler;
import io.papermc.paper.threadedregions.TickRegions;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import dev.bacteriawa.mint.config.modules.misc.TpsBarConfig;
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

import java.util.*;

public class GlobalServerTpsBar {
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
                cleanUp();
            }catch (Exception e){
                logger.error(e.getLocalizedMessage());
            }
        }, 1, TpsBarConfig.updateInterval);
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
        return ((CraftPlayer) player).getHandle().isTpsBarVisible;
    }

    public static void setVisibilityForPlayer(Player target,boolean canSee){
        ((CraftPlayer) target).getHandle().isTpsBarVisible = canSee;
    }

    private static void update(){
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


            BossBar targetBossbar = uuid2Bossbars.computeIfAbsent(
                    playerUUID,
                    unused -> BossBar.bossBar(Component.text(""),0.0F, BossBar.Color.valueOf(TpsBarConfig.tpsColors.get(3)), BossBar.Overlay.NOTCHED_20)
            );

            apiPlayer.showBossBar(targetBossbar);

            if (reportData != null){
                final TickData.SegmentData tpsData = reportData.tpsData().segmentAll();
                final double mspt = reportData.timePerTickData().segmentAll().average() / 1.0E6;

                updateTpsBar(tpsData.average(), mspt, targetBossbar, apiPlayer);
            }
        }, () -> {
            final BossBar removed = uuid2Bossbars.remove(playerUUID); // Auto clean up it

            if (removed != null) {
                apiPlayer.hideBossBar(removed);
            }
        }, 1, TpsBarConfig.updateInterval);
    }

    private static void updateTpsBar(double tps, double mspt, @NotNull BossBar bar, @NotNull Player player){
        bar.name(MiniMessage.miniMessage().deserialize(
                TpsBarConfig.tpsBarFormat,
                Placeholder.component("tps",getTpsComponent(tps)),
                Placeholder.component("mspt",getMsptComponent(mspt)),
                Placeholder.component("ping",getPingComponent(player.getPing())),
                Placeholder.component("chunkhot",getChunkHotComponent(player.getNearbyChunkHot()))
        ));
        bar.color(barColorFromTps(tps));
        bar.progress((float) Math.min((float)1,Math.max(mspt / 50,0)));
    }

    private static @NotNull Component getPingComponent(int ping){
        final BossBar.Color colorBukkit = barColorFromPing(ping);
        final String colorString = colorBukkit.name();

        final String content = "<%s><text></%s>";
        final String replaced = String.format(content,colorString,colorString);

        return MiniMessage.miniMessage().deserialize(replaced,Placeholder.parsed("text", String.valueOf(ping)));
    }

    private static BossBar.Color barColorFromPing(int ping){
        if (ping == -1){
            return BossBar.Color.valueOf(TpsBarConfig.pingColors.get(3));
        }

        if (ping <= 80){
            return BossBar.Color.valueOf(TpsBarConfig.pingColors.get(0));
        }

        if (ping <= 160){
            return BossBar.Color.valueOf(TpsBarConfig.pingColors.get(1));
        }

        return BossBar.Color.valueOf(TpsBarConfig.pingColors.get(2));
    }

    private static @NotNull Component getMsptComponent(double mspt){
        final BossBar.Color colorBukkit = barColorFromMspt(mspt);
        final String colorString = colorBukkit.name();

        final String content = "<%s><text></%s>";
        final String replaced = String.format(content,colorString,colorString);

        return MiniMessage.miniMessage().deserialize(replaced,Placeholder.parsed("text", String.format("%.2f", mspt)));
    }

    private static @NotNull Component getChunkHotComponent(long chunkHot){
        final BossBar.Color colorBukkit = barColorFromChunkHot(chunkHot);
        final String colorString = colorBukkit.name();

        final String content = "<%s><text></%s>";
        final String replaced = String.format(content,colorString,colorString);

        return MiniMessage.miniMessage().deserialize(replaced,Placeholder.parsed("text", String.valueOf(chunkHot)));
    }

    private static BossBar.Color barColorFromChunkHot(long chunkHot){
        if (chunkHot == -1){
            return BossBar.Color.valueOf(TpsBarConfig.chunkHotColors.get(3));
        }

        if (chunkHot <= 300000L){
            return BossBar.Color.valueOf(TpsBarConfig.chunkHotColors.get(0));
        }

        if (chunkHot <= 500000L){
            return BossBar.Color.valueOf(TpsBarConfig.chunkHotColors.get(1));
        }

        return BossBar.Color.valueOf(TpsBarConfig.chunkHotColors.get(2));
    }

    private static BossBar.Color barColorFromMspt(double mspt){
        if (mspt == -1){
            return BossBar.Color.valueOf(TpsBarConfig.tpsColors.get(3));
        }

        if (mspt <= 25){
            return BossBar.Color.valueOf(TpsBarConfig.tpsColors.get(0));
        }

        if (mspt <= 50){
            return BossBar.Color.valueOf(TpsBarConfig.tpsColors.get(1));
        }

        return BossBar.Color.valueOf(TpsBarConfig.tpsColors.get(2));
    }

    private static @NotNull Component getTpsComponent(double tps){
        final BossBar.Color colorBukkit = barColorFromTps(tps);
        final String colorString = colorBukkit.name();

        final String content = "<%s><text></%s>";
        final String replaced = String.format(content,colorString,colorString);

        return MiniMessage.miniMessage().deserialize(replaced,Placeholder.parsed("text", String.format("%.2f", tps)));
    }

    private static BossBar.Color barColorFromTps(double tps){
        if (tps == -1){
            return BossBar.Color.valueOf(TpsBarConfig.tpsColors.get(3));
        }

        if (tps >= 18){
            return BossBar.Color.valueOf(TpsBarConfig.tpsColors.get(0));
        }

        if (tps >= 15){
            return BossBar.Color.valueOf(TpsBarConfig.tpsColors.get(1));
        }

        return BossBar.Color.valueOf(TpsBarConfig.tpsColors.get(2));
    }
}