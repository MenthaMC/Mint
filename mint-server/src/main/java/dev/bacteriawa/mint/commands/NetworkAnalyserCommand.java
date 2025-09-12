package dev.bacteriawa.mint.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import dev.bacteriawa.mint.functions.NetworkAnalyser;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkAnalyserCommand extends MintSubCommand {
    private final Map<String, SubCommand> subcommands = new HashMap<>();

    public NetworkAnalyserCommand() {
        super("networkanalyser");
        this.setPermission("mint.networkanalyser");
        this.setUsage("/networkanalyser <start|stop|reset|view> [limit] - Network analysis tool for monitoring packet traffic");

        subcommands.put("start", new StartCommand());
        subcommands.put("stop", new StopCommand());
        subcommands.put("reset", new ResetCommand());
        subcommands.put("view", new ViewCommand());
        MintCommand.registerSubCommand(this);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage: /networkanalyser <start|stop|reset|view> [limit]").color(TextColor.color(255, 0, 0)));
            sender.sendMessage(Component.text("Use '/networkanalyser start' to begin packet analysis").color(TextColor.color(255, 0, 0)));
            sender.sendMessage(Component.text("Use '/networkanalyser view [limit]' to display collected data").color(TextColor.color(255, 0, 0)));
            return true;
        }
        
        String action = args[0].toLowerCase();
        SubCommand subCommand = subcommands.get(action);
        
        if (subCommand != null) {
            return subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        } else {
            sender.sendMessage(Component.text("Unknown action: " + action).color(TextColor.color(255, 0, 0)));
            sender.sendMessage(Component.text("Usage: /networkanalyser <start|stop|reset|view> [limit]").color(TextColor.color(255, 0, 0)));
            sender.sendMessage(Component.text("Use '/networkanalyser start' to begin packet analysis").color(TextColor.color(255, 0, 0)));
            sender.sendMessage(Component.text("Use '/networkanalyser view [limit]' to display collected data").color(TextColor.color(255, 0, 0)));
            return true;
        }
    }
    
    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("start", "stop", "reset", "view");
        }
        if (args.length == 2 && "view".equals(args[0].toLowerCase())) {
            return Arrays.asList("5", "7", "10", "15");
        }
        return List.of();
    }
    
    private interface SubCommand {
        boolean execute(CommandSender sender, String[] args);
    }
    
    private static class StartCommand implements SubCommand {
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (NetworkAnalyser.start()) {
                sender.sendMessage(Component.text("Started the analyser.").color(TextColor.color(0, 255, 0)));
            } else {
                sender.sendMessage(Component.text("The analyser is already running.").color(TextColor.color(255, 255, 0)));
            }
            return true;
        }
    }
    
    private static class StopCommand implements SubCommand {
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (NetworkAnalyser.stop()) {
                sender.sendMessage(Component.text("Stopped the analyser.").color(TextColor.color(0, 255, 0)));
            } else {
                sender.sendMessage(Component.text("The analyser is already stopped.").color(TextColor.color(0, 255, 0)));
            }
            return true;
        }
    }
    
    private static class ResetCommand implements SubCommand {
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            NetworkAnalyser.reset();
            sender.sendMessage(Component.text("Reset the analyser.").color(TextColor.color(0, 255, 0)));
            return true;
        }
    }
    
    private static class ViewCommand implements SubCommand {
        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (NetworkAnalyser.isEmpty()) {
                sender.sendMessage(Component.text("There's no data for view.").color(TextColor.color(255, 0, 0)));
                sender.sendMessage(Component.text("Use '/networkanalyser start' to begin collecting data").color(TextColor.color(255, 0, 0)));
                return true;
            }
            
            int limit = 7;
            if (args.length > 0) {
                try {
                    limit = Integer.parseInt(args[0]);
                    limit = Math.max(1, Math.min(limit, 20));
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Invalid limit value. Using default value 7.").color(TextColor.color(255, 0, 0)));
                }
            }

            sender.sendMessage(Component.text("----------------------------------------------------").color(TextColor.color(170, 170, 170)));
            sender.sendMessage(Component.text(" Network Analyser Results").color(TextColor.color(255, 170, 0)));
            sender.sendMessage(Component.text("----------------------------------------------------").color(TextColor.color(170, 170, 170)));
            sender.sendMessage(Component.text("Packet Type                    Count      Size (bps)      Size").color(TextColor.color(255, 255, 85)));
            sender.sendMessage(Component.text("----------------------------------------------------").color(TextColor.color(170, 170, 170)));
            
            Map<String, Long> sortedPackets = NetworkAnalyser.getSortedPacketSizes();
            
            int count = 0;
            for (Map.Entry<String, Long> entry : sortedPackets.entrySet()) {
                if (count++ >= limit) break;
                
                String packetType = entry.getKey();
                long size = entry.getValue();
                long counts = NetworkAnalyser.getPacketCount(packetType);
                
                // Convert bytes to bits per second
                long duration = NetworkAnalyser.getRunningTime();
                long bps = (duration > 0) ? (size * 8 * 1000) / duration : 0;
                
                // Format size with units
                String formattedSize = formatBytes(size);

                String formattedPacketType = packetType.length() > 30 ? packetType.substring(0, 27) + "..." : packetType;
                String formattedLine = String.format("%-30s x%-8d %-15s %s", 
                    formattedPacketType, 
                    counts,
                    formatBps(bps),
                    formattedSize);
                sender.sendMessage(Component.text(formattedLine).color(TextColor.color(255, 255, 255)));
            }
            
            long duration = NetworkAnalyser.getRunningTime();
            sender.sendMessage(Component.text("----------------------------------------------------").color(TextColor.color(170, 170, 170)));
            sender.sendMessage(Component.text("Analysis Duration: " + duration + "ms").color(TextColor.color(255, 170, 0)));
            sender.sendMessage(Component.text("----------------------------------------------------").color(TextColor.color(170, 170, 170)));
            
            return true;
        }
        
        private String formatBps(long bps) {
            if (bps < 1000) {
                return bps + " bps";
            } else if (bps < 1000000) {
                return String.format("%.2f Kbps", bps / 1000.0);
            } else {
                return String.format("%.2f Mbps", bps / 1000000.0);
            }
        }
        
        private String formatBytes(long bytes) {
            if (bytes < 1024) {
                return bytes + " B";
            } else if (bytes < 1024 * 1024) {
                return String.format("%.2f KB", bytes / 1024.0);
            } else if (bytes < 1024 * 1024 * 1024) {
                return String.format("%.2f MB", bytes / (1024.0 * 1024));
            } else {
                return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
            }
        }
    }
}