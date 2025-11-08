package dev.bacteriawa.mint.functions;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import dev.bacteriawa.mint.config.modules.misc.NetworkBarConfig;
import dev.bacteriawa.mint.utils.NullPlugin;
import dev.bacteriawa.mint.utils.MessageData;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static dev.bacteriawa.mint.config.modules.misc.NetworkBarConfig.trafficFormat;

public class GlobalServerNetworkBar {
    protected static final NullPlugin NULL_PLUGIN = new NullPlugin();
    protected static final Map<UUID, ScheduledTask> scheduledTasks = new HashMap<>();
    protected static volatile ScheduledTask scannerTask = null;
    private static final Logger logger = LogUtils.getLogger();

    private static final Map<UUID, NetworkStats> playerNetworkStats = Maps.newConcurrentMap();
    private static final AtomicLong totalBytesIn = new AtomicLong(0);
    private static final AtomicLong totalBytesOut = new AtomicLong(0);
    private static final AtomicLong totalPacketsIn = new AtomicLong(0);
    private static final AtomicLong totalPacketsOut = new AtomicLong(0);

    public static void init() {
        cancelBarUpdateTask();

        scannerTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(NULL_PLUGIN, unused -> {
            try {
                update();
                cleanUp();
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
            }
        }, 1, NetworkBarConfig.updateInterval);
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
        return ((CraftPlayer) player).getHandle().isNetworkBarVisible;
    }

    public static void setVisibilityForPlayer(Player target, boolean canSee) {
        ((CraftPlayer) target).getHandle().isNetworkBarVisible = canSee;
    }

    public static void updatePlayerNetworkData(Player player, long bytesIn, long bytesOut, long packetsIn, long packetsOut) {
        NetworkStats stats = playerNetworkStats.computeIfAbsent(player.getUniqueId(), k -> new NetworkStats());
        stats.bytesIn.addAndGet(bytesIn);
        stats.bytesOut.addAndGet(bytesOut);
        stats.packetsIn.addAndGet(packetsIn);
        stats.packetsOut.addAndGet(packetsOut);

        totalBytesIn.addAndGet(bytesIn);
        totalBytesOut.addAndGet(bytesOut);
        totalPacketsIn.addAndGet(packetsIn);
        totalPacketsOut.addAndGet(packetsOut);
    }

    public static void updateGlobalNetworkData(long bytesIn, long bytesOut, long packetsIn, long packetsOut) {
        totalBytesIn.addAndGet(bytesIn);
        totalBytesOut.addAndGet(bytesOut);
        totalPacketsIn.addAndGet(packetsIn);
        totalPacketsOut.addAndGet(packetsOut);
    }

    private static void update() {
        long bytesIn = totalBytesIn.getAndSet(0);
        long bytesOut = totalBytesOut.getAndSet(0);
        long packetsIn = totalPacketsIn.getAndSet(0);
        long packetsOut = totalPacketsOut.getAndSet(0);
        
        double intervalSeconds = NetworkBarConfig.updateInterval / 20.0;

        if (intervalSeconds <= 0) {
            intervalSeconds = 1.0;
        }

        double bitsPerSecondOut = (bytesOut * 8.0) / intervalSeconds;
        double bitsPerSecondIn = (bytesIn * 8.0) / intervalSeconds;
        double packetsPerSecondOut = packetsOut / intervalSeconds;
        double packetsPerSecondIn = packetsIn / intervalSeconds;

        String outgoingTraffic = formatBitsForTraffic(bitsPerSecondOut);
        String incomingTraffic = formatBitsForTraffic(bitsPerSecondIn);
        String outgoingPps = formatPps(packetsPerSecondOut);
        String incomingPps = formatPps(packetsPerSecondIn);

        MessageData messageData = new MessageData(trafficFormat);
        MessageData.ParsedMessageData parsedMessage = messageData.parsed(
                Placeholder.parsed("outgoing-traffic", outgoingTraffic),
                Placeholder.parsed("outgoing-pps", outgoingPps),
                Placeholder.parsed("incoming-traffic", incomingTraffic),
                Placeholder.parsed("incoming-pps", incomingPps)
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isPlayerVisible(player) && parsedMessage.getActionBar() != null) {
                player.sendActionBar(parsedMessage.getActionBar());
            }
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

    private static String formatBitsForTraffic(double bits) {
        if (bits < 1000) {
            return String.format("%.1f bps", bits);
        } else if (bits < 1000 * 1000) {
            return String.format("%.1f Kbps", bits / 1000.0);
        } else if (bits < 1000 * 1000 * 1000) {
            return String.format("%.1f Mbps", bits / (1000.0 * 1000));
        } else {
            return String.format("%.1f Gbps", bits / (1000.0 * 1000 * 1000));
        }
    }

    private static String formatPps(double pps) {
        if (pps < 1) {
            return String.format("%.2f pps", pps);
        } else if (pps < 1000) {
            return String.format("%.1f pps", pps);
        } else if (pps < 1000 * 1000) {
            return String.format("%.1f Kpps", pps / 1000.0);
        } else if (pps < 1000 * 1000 * 1000) {
            return String.format("%.1f Mpps", pps / (1000.0 * 1000));
        } else {
            return String.format("%.1f Gpps", pps / (1000.0 * 1000 * 1000));
        }
    }

    private static class NetworkStats {
        final AtomicLong bytesIn = new AtomicLong(0);
        final AtomicLong bytesOut = new AtomicLong(0);
        final AtomicLong packetsIn = new AtomicLong(0);
        final AtomicLong packetsOut = new AtomicLong(0);

    }
}