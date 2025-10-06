package dev.bacteriawa.mint.functions;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.Connection;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

public class NetworkAnalyser {
    private static boolean running = false;
    private static long startTime = 0;
    private static long stopTime = 0;
    
    private static final int MAX_TRACKED_CONNECTIONS = 10000;

    private static final Map<String, Object2IntMap<Connection>> packetConnections = new ConcurrentHashMap<>();
    private static final Object2LongMap<String> packetCounts = new Object2LongOpenHashMap<>();
    private static final Object2LongMap<String> packetSizes = new Object2LongOpenHashMap<>();

    private static final Object2LongMap<String> receivedPacketCounts = new Object2LongOpenHashMap<>();
    private static final Object2LongMap<String> receivedPacketSizes = new Object2LongOpenHashMap<>();
    private static final Object2LongMap<String> sentPacketCounts = new Object2LongOpenHashMap<>();
    private static final Object2LongMap<String> sentPacketSizes = new Object2LongOpenHashMap<>();
    private static final Object2IntMap<Connection> connectionPacketCounts = new Object2IntOpenHashMap<>();
    private static final Object2IntMap<Connection> connectionPacketSizes = new Object2IntOpenHashMap<>();
    
    static {
        packetCounts.defaultReturnValue(0L);
        packetSizes.defaultReturnValue(0L);
        receivedPacketCounts.defaultReturnValue(0L);
        receivedPacketSizes.defaultReturnValue(0L);
        sentPacketCounts.defaultReturnValue(0L);
        sentPacketSizes.defaultReturnValue(0L);
        connectionPacketCounts.defaultReturnValue(0);
        connectionPacketSizes.defaultReturnValue(0);
    }
    
    public static boolean start() {
        if (running) return false;
        running = true;
        reset();
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
    }
    
    public static void onPacketReceived(Connection connection, String packetType, int size) {
        if (!running || connection == null || packetType == null) return;
        
        try {
            packetCounts.mergeLong(packetType, 1L, Long::sum);
            packetSizes.mergeLong(packetType, size, Long::sum);
            
            receivedPacketCounts.mergeLong(packetType, 1L, Long::sum);
            receivedPacketSizes.mergeLong(packetType, size, Long::sum);
            
            if (connectionPacketCounts.size() < MAX_TRACKED_CONNECTIONS) {
                connectionPacketCounts.mergeInt(connection, 1, Integer::sum);
                connectionPacketSizes.mergeInt(connection, size, Integer::sum);
                
                packetConnections.computeIfAbsent(packetType, s -> new Object2IntOpenHashMap<>())
                              .put(connection, size);
            } else if (connectionPacketCounts.containsKey(connection)) {
                connectionPacketCounts.mergeInt(connection, 1, Integer::sum);
                connectionPacketSizes.mergeInt(connection, size, Integer::sum);
            }
        } catch (Exception e) {
        }
    }
    
    public static void onPacketSent(Connection connection, String packetType, int size) {
        if (!running || connection == null || packetType == null) return;
        
        try {
            packetCounts.mergeLong(packetType, 1L, Long::sum);
            packetSizes.mergeLong(packetType, size, Long::sum);
            
            sentPacketCounts.mergeLong(packetType, 1L, Long::sum);
            sentPacketSizes.mergeLong(packetType, size, Long::sum);
            
            if (connectionPacketCounts.size() < MAX_TRACKED_CONNECTIONS) {
                connectionPacketCounts.mergeInt(connection, 1, Integer::sum);
                connectionPacketSizes.mergeInt(connection, size, Integer::sum);
                
                packetConnections.computeIfAbsent(packetType, s -> new Object2IntOpenHashMap<>())
                              .put(connection, size);
            } else if (connectionPacketCounts.containsKey(connection)) {
                connectionPacketCounts.mergeInt(connection, 1, Integer::sum);
                connectionPacketSizes.mergeInt(connection, size, Integer::sum);
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
        return packetSizes.object2LongEntrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
    
    public static Map<String, Long> getSortedReceivedPacketSizes() {
        return receivedPacketSizes.object2LongEntrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
    
    public static Map<String, Long> getSortedSentPacketSizes() {
        return sentPacketSizes.object2LongEntrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
    
    public static Map<Connection, Integer> getSortedConnectionPacketCounts() {
        return connectionPacketCounts.object2IntEntrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
    
    public static Map<Connection, Integer> getSortedConnectionPacketSizes() {
        return connectionPacketSizes.object2IntEntrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
    
    public static long getPacketCount(String packetType) {
        return packetCounts.getLong(packetType);
    }
    
    public static long getReceivedPacketCount(String packetType) {
        return receivedPacketCounts.getLong(packetType);
    }
    
    public static long getSentPacketCount(String packetType) {
        return sentPacketCounts.getLong(packetType);
    }
    
    public static long getPacketSize(String packetType) {
        return packetSizes.getLong(packetType);
    }
    
    public static long getReceivedPacketSize(String packetType) {
        return receivedPacketSizes.getLong(packetType);
    }
    
    public static long getSentPacketSize(String packetType) {
        return sentPacketSizes.getLong(packetType);
    }
    
    public static int getConnectionPacketCount(Connection connection) {
        return connectionPacketCounts.getInt(connection);
    }
    
    public static int getConnectionPacketSize(Connection connection) {
        return connectionPacketSizes.getInt(connection);
    }
    
    public static boolean isEmpty() {
        return packetCounts.isEmpty();
    }
    
    public static Map<String, Long> getAllPacketCounts() {
        return new Object2LongOpenHashMap<>(packetCounts);
    }
    
    public static Map<String, Long> getAllPacketSizes() {
        return new Object2LongOpenHashMap<>(packetSizes);
    }
    
    public static void removeConnection(Connection connection) {
        if (connection == null) return;
        
        try {
            connectionPacketCounts.removeInt(connection);
            connectionPacketSizes.removeInt(connection);
            
            for (Object2IntMap<Connection> map : packetConnections.values()) {
                if (map != null) {
                    map.removeInt(connection);
                }
            }
        } catch (Exception e) {
        }
    }
    
    public static int getActiveConnectionCount() {
        return connectionPacketCounts.size();
    }
    
    public static long getTotalPacketCount() {
        return packetCounts.values().longStream().sum();
    }
    
    public static long getTotalPacketSize() {
        return packetSizes.values().longStream().sum();
    }
    
    public static long getTotalReceivedPacketCount() {
        return receivedPacketCounts.values().longStream().sum();
    }
    
    public static long getTotalReceivedPacketSize() {
        return receivedPacketSizes.values().longStream().sum();
    }
    
    public static long getTotalSentPacketCount() {
        return sentPacketCounts.values().longStream().sum();
    }
    
    public static long getTotalSentPacketSize() {
        return sentPacketSizes.values().longStream().sum();
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
        java.util.Set<String> types = new java.util.HashSet<>();
        for (Map.Entry<String, Object2IntMap<Connection>> entry : packetConnections.entrySet()) {
            if (entry.getValue().containsKey(connection)) {
                types.add(entry.getKey());
            }
        }
        return types;
    }
}