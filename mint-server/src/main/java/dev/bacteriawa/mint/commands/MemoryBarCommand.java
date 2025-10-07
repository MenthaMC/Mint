package dev.bacteriawa.mint.commands;

import dev.bacteriawa.mint.config.modules.misc.MembarConfig;
import dev.bacteriawa.mint.functions.GlobalServerMemoryBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MemoryBarCommand extends MintSubCommand {
    public MemoryBarCommand() {
        super("membar", MintCommand.MINT_ADMIN_PERMISSION);
        if (false) this.setPermission("mint.membar");
        this.setUsage("/membar");
        MintCommand.registerSubCommand(this);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!testPermission(sender)) {
            return true;
        }

        if (!MembarConfig.memoryBarEnabled) {
            sender.sendMessage(Component.text("MemoryBar was already disabled!").color(TextColor.color(255,0,0)));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only player can use this command!").color(TextColor.color(255,0,0)));
            return true;
        }

        if (GlobalServerMemoryBar.isPlayerVisible(player)) {
            player.sendMessage(Component.text("Disabled Memory bar").color(TextColor.color(170, 170, 255)));
            GlobalServerMemoryBar.setVisibilityForPlayer(player, false);
            return true;
        }

        player.sendMessage(Component.text("Enabled Memory bar").color(TextColor.color(170, 170, 255)));
        GlobalServerMemoryBar.setVisibilityForPlayer(player, true);

        return true;
    }
}
