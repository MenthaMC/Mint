package dev.bacteriawa.mint.functions;

import net.minecraft.network.Connection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

public class NetworkAnalyser {
    private static volatile boolean running = false;
    private static volatile long startTime = 0;
    private static volatile long stopTime = 0;
    
    private static final int MAX_TRACKED_CONNECTIONS = 10000;

    private static final ConcurrentHashMap<String, ConcurrentHashMap<Connection, Boolean>> packetConnections = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, LongAdder> packetCounts = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, LongAdder> packetSizes = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, LongAdder> receivedPacketCounts = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, LongAdder> receivedPacketSizes = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, LongAdder> sentPacketCounts = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, LongAdder> sentPacketSizes = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Connection, LongAdder> connectionPacketCounts = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Connection, LongAdder> connectionPacketSizes = new ConcurrentHashMap<>();
    private static final AtomicInteger trackedConnections = new AtomicInteger(0);
    
    public static boolean start() {
        if (running) return false;
        reset();
        running = true;
        return true;
    }
    
    public static boolean stop() {
        if (!running) return false;
        running = false;
        stopTime = System.currentTimeMillis();
        return true;
    }
    
    public static void reset() {
        startTime = System.currentTimeMillis();
        packetCounts.clear();
        packetSizes.clear();
        packetConnections.clear();
        receivedPacketCounts.clear();
        receivedPacketSizes.clear();
        sentPacketCounts.clear();
        sentPacketSizes.clear();
        connectionPacketCounts.clear();
        connectionPacketSizes.clear();
        trackedConnections.set(0);
    }
    
    public static void onPacketReceived(Connection connection, String packetType, int size) {
        if (!running || connection == null || packetType == null) return;
        
        try {
            addPacketStats(packetCounts, packetSizes, packetType, size);
            addPacketStats(receivedPacketCounts, receivedPacketSizes, packetType, size);

            if (trackConnection(connection)) {
                LongAdder countAdder = connectionPacketCounts.get(connection);
                LongAdder sizeAdder = connectionPacketSizes.get(connection);
                if (countAdder != null) {
                    countAdder.increment();
                }
                if (sizeAdder != null) {
                    sizeAdder.add(size);
                }

                packetConnections.computeIfAbsent(packetType, s -> new ConcurrentHashMap<>())
                    .putIfAbsent(connection, Boolean.TRUE);
            }
        } catch (Exception e) {
        }
    }
    
    public static void onPacketSent(Connection connection, String packetType, int size) {
        if (!running || connection == null || packetType == null) return;
        
        try {
            addPacketStats(packetCounts, packetSizes, packetType, size);
            addPacketStats(sentPacketCounts, sentPacketSizes, packetType, size);

            if (trackConnection(connection)) {
                LongAdder countAdder = connectionPacketCounts.get(connection);
                LongAdder sizeAdder = connectionPacketSizes.get(connection);
                if (countAdder != null) {
                    countAdder.increment();
                }
                if (sizeAdder != null) {
                    sizeAdder.add(size);
                }

                packetConnections.computeIfAbsent(packetType, s -> new ConcurrentHashMap<>())
                    .putIfAbsent(connection, Boolean.TRUE);
            }
        } catch (Exception e) {
        }
    }
    
    public static boolean isRunning() {
        return running;
    }
    
    public static long getRunningTime() {
        return (running ? System.currentTimeMillis() - startTime : stopTime - startTime);
    }
    
    public static Map<String, Long> getSortedPacketSizes() {
        return getSortedLongMap(packetSizes);
    }
    
    public static Map<String, Long> getSortedReceivedPacketSizes() {
        return getSortedLongMap(receivedPacketSizes);
    }
    
    public static Map<String, Long> getSortedSentPacketSizes() {
        return getSortedLongMap(sentPacketSizes);
    }
    
    public static Map<Connection, Integer> getSortedConnectionPacketCounts() {
        return getSortedConnectionMap(connectionPacketCounts);
    }
    
    public static Map<Connection, Integer> getSortedConnectionPacketSizes() {
        return getSortedConnectionMap(connectionPacketSizes);
    }
    
    public static long getPacketCount(String packetType) {
        LongAdder adder = packetCounts.get(packetType);
        return adder == null ? 0L : adder.sum();
    }
    
    public static long getReceivedPacketCount(String packetType) {
        LongAdder adder = receivedPacketCounts.get(packetType);
        return adder == null ? 0L : adder.sum();
    }
    
    public static long getSentPacketCount(String packetType) {
        LongAdder adder = sentPacketCounts.get(packetType);
        return adder == null ? 0L : adder.sum();
    }
    
    public static long getPacketSize(String packetType) {
        LongAdder adder = packetSizes.get(packetType);
        return adder == null ? 0L : adder.sum();
    }
    
    public static long getReceivedPacketSize(String packetType) {
        LongAdder adder = receivedPacketSizes.get(packetType);
        return adder == null ? 0L : adder.sum();
    }
    
    public static long getSentPacketSize(String packetType) {
        LongAdder adder = sentPacketSizes.get(packetType);
        return adder == null ? 0L : adder.sum();
    }
    
    public static int getConnectionPacketCount(Connection connection) {
        LongAdder adder = connectionPacketCounts.get(connection);
        return adder == null ? 0 : (int) adder.sum();
    }
    
    public static int getConnectionPacketSize(Connection connection) {
        LongAdder adder = connectionPacketSizes.get(connection);
        return adder == null ? 0 : (int) adder.sum();
    }
    
    public static boolean isEmpty() {
        return packetCounts.isEmpty();
    }
    
    public static Map<String, Long> getAllPacketCounts() {
        return snapshotLongMap(packetCounts);
    }
    
    public static Map<String, Long> getAllPacketSizes() {
        return snapshotLongMap(packetSizes);
    }
    
    public static void removeConnection(Connection connection) {
        if (connection == null) return;
        
        try {
            if (connectionPacketCounts.remove(connection) != null) {
                trackedConnections.decrementAndGet();
            }
            connectionPacketSizes.remove(connection);
            
            for (ConcurrentHashMap<Connection, Boolean> map : packetConnections.values()) {
                if (map != null) {
                    map.remove(connection);
                }
            }
        } catch (Exception e) {
        }
    }
    
    public static int getActiveConnectionCount() {
        return connectionPacketCounts.size();
    }
    
    public static long getTotalPacketCount() {
        return sumValues(packetCounts);
    }
    
    public static long getTotalPacketSize() {
        return sumValues(packetSizes);
    }
    
    public static long getTotalReceivedPacketCount() {
        return sumValues(receivedPacketCounts);
    }
    
    public static long getTotalReceivedPacketSize() {
        return sumValues(receivedPacketSizes);
    }
    
    public static long getTotalSentPacketCount() {
        return sumValues(sentPacketCounts);
    }
    
    public static long getTotalSentPacketSize() {
        return sumValues(sentPacketSizes);
    }
    
    public static double getAveragePacketsPerSecond() {
        long runtime = getRunningTime();
        if (runtime == 0) return 0;
        return (getTotalPacketCount() * 1000.0) / runtime;
    }
    
    public static double getAverageBytesPerSecond() {
        long runtime = getRunningTime();
        if (runtime == 0) return 0;
        return (getTotalPacketSize() * 1000.0) / runtime;
    }
    
    public static java.util.Set<String> getPacketTypesForConnection(Connection connection) {
        java.util.Set<String> types = new HashSet<>();
        for (Map.Entry<String, ConcurrentHashMap<Connection, Boolean>> entry : packetConnections.entrySet()) {
            if (entry.getValue().containsKey(connection)) {
                types.add(entry.getKey());
            }
        }
        return types;
    }

    private static void addPacketStats(ConcurrentHashMap<String, LongAdder> counts,
                                       ConcurrentHashMap<String, LongAdder> sizes,
                                       String packetType,
                                       int size) {
        counts.computeIfAbsent(packetType, key -> new LongAdder()).increment();
        sizes.computeIfAbsent(packetType, key -> new LongAdder()).add(size);
    }

    private static boolean trackConnection(Connection connection) {
        if (connectionPacketCounts.containsKey(connection)) {
            return true;
        }
        if (trackedConnections.get() >= MAX_TRACKED_CONNECTIONS) {
            return false;
        }
        if (connectionPacketCounts.putIfAbsent(connection, new LongAdder()) == null) {
            trackedConnections.incrementAndGet();
        }
        connectionPacketSizes.putIfAbsent(connection, new LongAdder());
        return true;
    }

    private static Map<String, Long> getSortedLongMap(ConcurrentHashMap<String, LongAdder> map) {
        return map.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue().sum(), a.getValue().sum()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().sum(),
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    private static Map<Connection, Integer> getSortedConnectionMap(ConcurrentHashMap<Connection, LongAdder> map) {
        return map.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue().sum(), a.getValue().sum()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> (int) entry.getValue().sum(),
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    private static Map<String, Long> snapshotLongMap(ConcurrentHashMap<String, LongAdder> map) {
        Map<String, Long> snapshot = new HashMap<>();
        for (Map.Entry<String, LongAdder> entry : map.entrySet()) {
            snapshot.put(entry.getKey(), entry.getValue().sum());
        }
        return snapshot;
    }

    private static long sumValues(ConcurrentHashMap<?, LongAdder> map) {
        long total = 0L;
        for (LongAdder adder : map.values()) {
            total += adder.sum();
        }
        return total;
    }
}
