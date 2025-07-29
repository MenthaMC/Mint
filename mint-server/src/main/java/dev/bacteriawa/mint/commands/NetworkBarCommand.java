package dev.bacteriawa.mint.commands;

import dev.bacteriawa.mint.config.modules.misc.NetworkBarConfig;
import dev.bacteriawa.mint.functions.GlobalServerNetworkBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class NetworkBarCommand extends BukkitCommand {
    private final Map<String, MintSubCommand> subcommands = new HashMap<>();

    public NetworkBarCommand() {
        super("networkbar");
        this.setPermission("mint.networkbar");
        this.setUsage("/networkbar");

        subcommands.put("networkbar", new MintSubCommand());
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!testPermission(sender)) {
            return true;
        }

        if (!NetworkBarConfig.networkBarEnabled) {
            sender.sendMessage(Component.text("Network bar is disabled in config!").color(TextColor.color(255, 0, 0)));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only player can use this command!").color(TextColor.color(255, 0, 0)));
            return true;
        }

        if (GlobalServerNetworkBar.isPlayerVisible(player)) {
            player.sendMessage(Component.text("Disabled network bar").color(TextColor.color(0, 255, 0)));
            GlobalServerNetworkBar.setVisibilityForPlayer(player, false);
            return true;
        }

        player.sendMessage(Component.text("Enabled network bar").color(TextColor.color(0, 255, 0)));
        GlobalServerNetworkBar.setVisibilityForPlayer(player, true);

        return true;
    }
}