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
        if (!running) return;
        
        packetCounts.put(packetType, packetCounts.getLong(packetType) + 1);
        packetSizes.put(packetType, packetSizes.getLong(packetType) + size);
        
        // 更新接收数据包统计
        receivedPacketCounts.put(packetType, receivedPacketCounts.getLong(packetType) + 1);
        receivedPacketSizes.put(packetType, receivedPacketSizes.getLong(packetType) + size);
        
        // 更新连接统计
        connectionPacketCounts.put(connection, connectionPacketCounts.getInt(connection) + 1);
        connectionPacketSizes.put(connection, connectionPacketSizes.getInt(connection) + size);
        
        packetConnections.computeIfAbsent(packetType, s -> new Object2IntOpenHashMap<>())
                      .put(connection, size);
    }
    
    public static void onPacketSent(Connection connection, String packetType, int size) {
        if (!running) return;
        
        packetCounts.put(packetType, packetCounts.getLong(packetType) + 1);
        packetSizes.put(packetType, packetSizes.getLong(packetType) + size);
        
        // 更新发送数据包统计
        sentPacketCounts.put(packetType, sentPacketCounts.getLong(packetType) + 1);
        sentPacketSizes.put(packetType, sentPacketSizes.getLong(packetType) + size);
        
        // 更新连接统计
        connectionPacketCounts.put(connection, connectionPacketCounts.getInt(connection) + 1);
        connectionPacketSizes.put(connection, connectionPacketSizes.getInt(connection) + size);
        
        packetConnections.computeIfAbsent(packetType, s -> new Object2IntOpenHashMap<>())
                      .put(connection, size);
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
    }// TODO : 获取按接收数据包大小排序的映射
    
    /**
     * 获取按接收数据包大小排序的映射
     * 引用: 返回按值降序排列的接收数据包大小映射
     * 
     * @return 按接收数据包大小降序排列的映射
     */
    public static Map<String, Long> getSortedSentPacketSizes() {
        return sentPacketSizes.object2LongEntrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }// TODO : 获取按发送数据包大小排序的映射
    
    /**
     * 获取按发送数据包大小排序的映射
     * 引用: 返回按值降序排列的发送数据包大小映射
     * 
     * @return 按发送数据包大小降序排列的映射
     */
    public static Map<Connection, Integer> getSortedConnectionPacketCounts() {
        return connectionPacketCounts.object2IntEntrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    } // TODO : 获取按连接数据包计数排序的映射
    
    /**
     * 获取按连接数据包计数排序的映射
     * 引用: 返回按值降序排列的连接数据包计数映射
     * 
     * @return 按连接数据包计数降序排列的映射
     */
    public static Map<Connection, Integer> getSortedConnectionPacketSizes() {
        return connectionPacketSizes.object2IntEntrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    } // TODO : 获取按连接数据包大小排序的映射
    
    /**
     * 获取数据包类型的总数量
     * 引用: 返回指定数据包类型的总数量
     * 
     * @param packetType 数据包类型名称
     * @return 数据包类型的总数量
     */
    public static long getPacketCount(String packetType) {
        return packetCounts.getLong(packetType);
    } // TODO : 获取数据包类型的总大小
    
    /**
     * 获取接收数据包类型的数量
     * 引用: 返回指定接收数据包类型的数量
     * 
     * @param packetType 数据包类型名称
     * @return 接收数据包类型的数量
     */
    public static long getReceivedPacketCount(String packetType) {
        return receivedPacketCounts.getLong(packetType);
    } // TODO : 获取接收数据包类型的总大小
    
    /**
     * 获取发送数据包类型的数量
     * 引用: 返回指定发送数据包类型的数量
     * 
     * @param packetType 数据包类型名称
     * @return 发送数据包类型的数量
     */
    public static long getSentPacketCount(String packetType) {
        return sentPacketCounts.getLong(packetType);
    } // TODO : 获取发送数据包类型的总大小
    
    /**
     * 获取数据包类型的总大小
     * 引用: 返回指定数据包类型的总大小（字节）
     * 
     * @param packetType 数据包类型名称
     * @return 数据包类型的总大小
     */
    public static long getPacketSize(String packetType) {
        return packetSizes.getLong(packetType);
    } // TODO : 获取数据包类型的总大小
    
    /**
     * 获取接收数据包类型的大小
     * 引用: 返回指定接收数据包类型的大小（字节）
     * 
     * @param packetType 数据包类型名称
     * @return 接收数据包类型的大小
     */
    public static long getReceivedPacketSize(String packetType) {
        return receivedPacketSizes.getLong(packetType);
    } // TODO : 获取接收数据包类型的总大小
    
    /**
     * 获取发送数据包类型的大小
     * 引用: 返回指定发送数据包类型的大小（字节）
     * 
     * @param packetType 数据包类型名称
     * @return 发送数据包类型的大小
     */
    public static long getSentPacketSize(String packetType) {
        return sentPacketSizes.getLong(packetType);
    } // TODO : 获取发送数据包类型的总大小
    
    /**
     * 获取连接的数据包计数
     * 引用: 返回指定连接的数据包计数
     * 
     * @param connection 连接对象
     * @return 连接的数据包计数
     */
    public static int getConnectionPacketCount(Connection connection) {
        return connectionPacketCounts.getInt(connection);
    } // TODO : 获取连接的数据包大小
    
    /**
     * 获取连接的数据包总大小
     * 引用: 返回指定连接的数据包总大小（字节）
     * 
     * @param connection 连接对象
     * @return 连接的数据包总大小
     */
    public static int getConnectionPacketSize(Connection connection) {
        return connectionPacketSizes.getInt(connection);
    } // TODO : 获取连接的数据包类型

    public static boolean isEmpty() {
        return packetCounts.isEmpty();
    }
}