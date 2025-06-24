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

public class MemoryBarCommand extends BukkitCommand {
    private final Map<String, MintSubCommand> subcommands = new HashMap<>();

    public MemoryBarCommand() {
        super("membar");
        this.setPermission("mint.membar");
        this.setUsage("/membar");

        subcommands.put("membar", new MintSubCommand());
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
            player.sendMessage(Component.text("Disabled MemoryBar").color(TextColor.color(0,255,0)));
            GlobalServerMemoryBar.setVisibilityForPlayer(player,false);
            return true;
        }

        player.sendMessage(Component.text("Enabled MemoryBar").color(TextColor.color(0,255,0)));
        GlobalServerMemoryBar.setVisibilityForPlayer(player,true);

        return true;
    }
}
