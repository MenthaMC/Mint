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
    
    // 最大跟踪连接数，防止内存泄漏（如果超过此数量，停止跟踪新连接）
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
    
    /**
     * 启动网络分析器
     * 
     * @return 如果成功启动返回 true，如果已经在运行则返回 false
     */
    public static boolean start() {
        if (running) return false;
        running = true;
        reset();
        return true;
    }
    
    /**
     * 停止网络分析器
     * 
     * @return 如果成功停止返回 true，如果未在运行则返回 false
     */
    public static boolean stop() {
        if (!running) return false;
        running = false;
        stopTime = System.currentTimeMillis();
        return true;
    }
    
    /**
     * 重置所有统计数据并重新开始计时
     */
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
    
    /**
     * 记录接收到的数据包（线程安全）
     * 
     * @param connection 连接对象
     * @param packetType 数据包类型名称
     * @param size 数据包大小（字节）
     */
    public static void onPacketReceived(Connection connection, String packetType, int size) {
        if (!running || connection == null || packetType == null) return;
        
        try {
            // 使用原子操作更新统计数据
            packetCounts.mergeLong(packetType, 1L, Long::sum);
            packetSizes.mergeLong(packetType, size, Long::sum);
            
            receivedPacketCounts.mergeLong(packetType, 1L, Long::sum);
            receivedPacketSizes.mergeLong(packetType, size, Long::sum);
            
            // 防止内存泄漏：限制跟踪的连接数量
            if (connectionPacketCounts.size() < MAX_TRACKED_CONNECTIONS) {
                connectionPacketCounts.mergeInt(connection, 1, Integer::sum);
                connectionPacketSizes.mergeInt(connection, size, Integer::sum);
                
                packetConnections.computeIfAbsent(packetType, s -> new Object2IntOpenHashMap<>())
                              .put(connection, size);
            } else if (connectionPacketCounts.containsKey(connection)) {
                // 只更新已存在的连接
                connectionPacketCounts.mergeInt(connection, 1, Integer::sum);
                connectionPacketSizes.mergeInt(connection, size, Integer::sum);
            }
        } catch (Exception e) {
            // Silently ignore to prevent disrupting packet processing
        }
    }
    
    /**
     * 记录发送的数据包（线程安全）
     * 
     * @param connection 连接对象
     * @param packetType 数据包类型名称
     * @param size 数据包大小（字节）
     */
    public static void onPacketSent(Connection connection, String packetType, int size) {
        if (!running || connection == null || packetType == null) return;
        
        try {
            // 使用原子操作更新统计数据
            packetCounts.mergeLong(packetType, 1L, Long::sum);
            packetSizes.mergeLong(packetType, size, Long::sum);
            
            sentPacketCounts.mergeLong(packetType, 1L, Long::sum);
            sentPacketSizes.mergeLong(packetType, size, Long::sum);
            
            // 防止内存泄漏：限制跟踪的连接数量
            if (connectionPacketCounts.size() < MAX_TRACKED_CONNECTIONS) {
                connectionPacketCounts.mergeInt(connection, 1, Integer::sum);
                connectionPacketSizes.mergeInt(connection, size, Integer::sum);
                
                packetConnections.computeIfAbsent(packetType, s -> new Object2IntOpenHashMap<>())
                              .put(connection, size);
            } else if (connectionPacketCounts.containsKey(connection)) {
                // 只更新已存在的连接
                connectionPacketCounts.mergeInt(connection, 1, Integer::sum);
                connectionPacketSizes.mergeInt(connection, size, Integer::sum);
            }
        } catch (Exception e) {
            // Silently ignore to prevent disrupting packet processing
        }
    }
    
    /**
     * 检查网络分析器是否正在运行
     * 
     * @return 如果正在运行返回 true
     */
    public static boolean isRunning() {
        return running;
    }
    
    /**
     * 获取运行时长（单位：毫秒）
     * 
     * @return 如果正在运行则返回从开始到现在的时长，否则返回总运行时长
     */
    public static long getRunningTime() {
        return (running ? System.currentTimeMillis() - startTime : stopTime - startTime);
    }
    
    /**
     * 获取按数据包总大小排序的映射（接收+发送）
     * 
     * @return 按数据包大小降序排列的映射
     */
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
    
    /**
     * 获取按接收数据包大小排序的映射
     * 
     * @return 按接收数据包大小降序排列的映射
     */
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
    
    /**
     * 获取按发送数据包大小排序的映射
     * 
     * @return 按发送数据包大小降序排列的映射
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
    }
    
    /**
     * 获取按连接数据包计数排序的映射
     * 
     * @return 按连接数据包计数降序排列的映射
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
    }
    
    /**
     * 获取按连接数据包大小排序的映射
     * 
     * @return 按连接数据包大小降序排列的映射
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
    }
    
    /**
     * 获取数据包类型的总数量（接收+发送）
     * 
     * @param packetType 数据包类型名称
     * @return 数据包类型的总数量
     */
    public static long getPacketCount(String packetType) {
        return packetCounts.getLong(packetType);
    }
    
    /**
     * 获取接收数据包类型的数量
     * 
     * @param packetType 数据包类型名称
     * @return 接收数据包类型的数量
     */
    public static long getReceivedPacketCount(String packetType) {
        return receivedPacketCounts.getLong(packetType);
    }
    
    /**
     * 获取发送数据包类型的数量
     * 
     * @param packetType 数据包类型名称
     * @return 发送数据包类型的数量
     */
    public static long getSentPacketCount(String packetType) {
        return sentPacketCounts.getLong(packetType);
    }
    
    /**
     * 获取数据包类型的总大小（接收+发送，单位：字节）
     * 
     * @param packetType 数据包类型名称
     * @return 数据包类型的总大小
     */
    public static long getPacketSize(String packetType) {
        return packetSizes.getLong(packetType);
    }
    
    /**
     * 获取接收数据包类型的大小（单位：字节）
     * 
     * @param packetType 数据包类型名称
     * @return 接收数据包类型的大小
     */
    public static long getReceivedPacketSize(String packetType) {
        return receivedPacketSizes.getLong(packetType);
    }
    
    /**
     * 获取发送数据包类型的大小（单位：字节）
     * 
     * @param packetType 数据包类型名称
     * @return 发送数据包类型的大小
     */
    public static long getSentPacketSize(String packetType) {
        return sentPacketSizes.getLong(packetType);
    }
    
    /**
     * 获取连接的数据包计数
     * 
     * @param connection 连接对象
     * @return 连接的数据包计数
     */
    public static int getConnectionPacketCount(Connection connection) {
        return connectionPacketCounts.getInt(connection);
    }
    
    /**
     * 获取连接的数据包总大小（单位：字节）
     * 
     * @param connection 连接对象
     * @return 连接的数据包总大小
     */
    public static int getConnectionPacketSize(Connection connection) {
        return connectionPacketSizes.getInt(connection);
    }
    
    /**
     * 检查是否没有收集到任何数据包统计
     * 
     * @return 如果没有统计数据则返回 true
     */
    public static boolean isEmpty() {
        return packetCounts.isEmpty();
    }
    
    // ==================== 新增功能方法 ====================
    
    /**
     * 获取所有数据包类型的统计数据（不可变副本）
     * 
     * @return 数据包类型到计数的映射
     */
    public static Map<String, Long> getAllPacketCounts() {
        return new Object2LongOpenHashMap<>(packetCounts);
    }
    
    /**
     * 获取所有数据包类型的大小统计（不可变副本）
     * 
     * @return 数据包类型到大小的映射
     */
    public static Map<String, Long> getAllPacketSizes() {
        return new Object2LongOpenHashMap<>(packetSizes);
    }
    
    /**
     * 移除指定连接的统计数据（用于清理已断开的连接）
     * 防止内存泄漏
     * 
     * @param connection 要移除的连接
     */
    public static void removeConnection(Connection connection) {
        if (connection == null) return;
        
        try {
            // 从连接统计中移除
            connectionPacketCounts.removeInt(connection);
            connectionPacketSizes.removeInt(connection);
            
            // 从所有数据包类型的连接映射中移除
            // 使用迭代器避免并发修改异常
            for (Object2IntMap<Connection> map : packetConnections.values()) {
                if (map != null) {
                    map.removeInt(connection);
                }
            }
        } catch (Exception e) {
            // Silently ignore cleanup errors
        }
    }
    
    /**
     * 获取当前活跃的连接数量
     * 
     * @return 活跃连接数
     */
    public static int getActiveConnectionCount() {
        return connectionPacketCounts.size();
    }
    
    /**
     * 获取总数据包数量（接收+发送）
     * 
     * @return 总数据包数量
     */
    public static long getTotalPacketCount() {
        return packetCounts.values().longStream().sum();
    }
    
    /**
     * 获取总数据包大小（接收+发送，单位：字节）
     * 
     * @return 总数据包大小
     */
    public static long getTotalPacketSize() {
        return packetSizes.values().longStream().sum();
    }
    
    /**
     * 获取总接收数据包数量
     * 
     * @return 总接收数据包数量
     */
    public static long getTotalReceivedPacketCount() {
        return receivedPacketCounts.values().longStream().sum();
    }
    
    /**
     * 获取总接收数据包大小（单位：字节）
     * 
     * @return 总接收数据包大小
     */
    public static long getTotalReceivedPacketSize() {
        return receivedPacketSizes.values().longStream().sum();
    }
    
    /**
     * 获取总发送数据包数量
     * 
     * @return 总发送数据包数量
     */
    public static long getTotalSentPacketCount() {
        return sentPacketCounts.values().longStream().sum();
    }
    
    /**
     * 获取总发送数据包大小（单位：字节）
     * 
     * @return 总发送数据包大小
     */
    public static long getTotalSentPacketSize() {
        return sentPacketSizes.values().longStream().sum();
    }
    
    /**
     * 获取平均每秒数据包数量
     * 
     * @return 平均每秒数据包数量，如果运行时间为0则返回0
     */
    public static double getAveragePacketsPerSecond() {
        long runtime = getRunningTime();
        if (runtime == 0) return 0;
        return (getTotalPacketCount() * 1000.0) / runtime;
    }
    
    /**
     * 获取平均每秒数据传输速率（单位：字节/秒）
     * 
     * @return 平均每秒传输的字节数，如果运行时间为0则返回0
     */
    public static double getAverageBytesPerSecond() {
        long runtime = getRunningTime();
        if (runtime == 0) return 0;
        return (getTotalPacketSize() * 1000.0) / runtime;
    }
    
    /**
     * 获取指定连接的所有数据包类型
     * 
     * @param connection 连接对象
     * @return 该连接涉及的所有数据包类型集合
     */
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