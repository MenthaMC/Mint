package dev.bacteriawa.mint.commands;

import dev.bacteriawa.mint.config.modules.misc.RegionBarConfig;
import dev.bacteriawa.mint.functions.GlobalServerRegionBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RegionBarCommand extends MintSubCommand {
    public RegionBarCommand() {
        super("regionbar");
        this.setPermission("mint.regionbar");
        this.setUsage("/regionbar");

        MintCommand.registerSubCommand(this);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!testPermission(sender)) {
            return true;
        }

        if (!RegionBarConfig.regionbarEnabled) {
            sender.sendMessage(Component.text("Regionbar was already disabled!").color(TextColor.color(255, 0, 0)));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only player can use this command!").color(TextColor.color(255, 0, 0)));
            return true;
        }

        if (GlobalServerRegionBar.isPlayerVisible(player)) {
            player.sendMessage(Component.text("Disabled region bar").color(TextColor.color(0, 255, 0)));
            GlobalServerRegionBar.setVisibilityForPlayer(player, false);
            return true;
        }

        player.sendMessage(Component.text("Enabled region bar").color(TextColor.color(0, 255, 0)));
        GlobalServerRegionBar.setVisibilityForPlayer(player, true);

        return true;
    }
}