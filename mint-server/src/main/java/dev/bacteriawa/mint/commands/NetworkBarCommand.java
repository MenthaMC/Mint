package dev.bacteriawa.mint.commands;

import dev.bacteriawa.mint.config.modules.misc.NetworkBarConfig;
import dev.bacteriawa.mint.functions.GlobalServerNetworkBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NetworkBarCommand extends MintSubCommand {
    public NetworkBarCommand() {
        super("networkbar");
        this.setPermission("mint.networkbar");
        this.setUsage("/networkbar");

        MintCommand.registerSubCommand(this);
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