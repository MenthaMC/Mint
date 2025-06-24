package dev.bacteriawa.mint.commands;

import dev.bacteriawa.mint.config.modules.misc.TpsBarConfig;
import dev.bacteriawa.mint.functions.GlobalServerTpsBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TpsBarCommand extends BukkitCommand {
    private final Map<String, MintSubCommand> subcommands = new HashMap<>();

    public TpsBarCommand() {
        super("tpsbar");
        this.setPermission("mint.tpsbar");
        this.setUsage("/tpsbar");

        subcommands.put("tpsbar", new MintSubCommand());
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!testPermission(sender)) {
            return true;
        }

        if (!TpsBarConfig.tpsbarEnabled) {
            sender.sendMessage(Component.text("Tpsbar was already disabled!").color(TextColor.color(255, 0, 0)));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only player can use this command!").color(TextColor.color(255, 0, 0)));
            return true;
        }

        if (GlobalServerTpsBar.isPlayerVisible(player)) {
            player.sendMessage(Component.text("Disabled Tpsbar").color(TextColor.color(0, 255, 0)));
            GlobalServerTpsBar.setVisibilityForPlayer(player, false);
            return true;
        }

        player.sendMessage(Component.text("Enabled Tpsbar").color(TextColor.color(0, 255, 0)));
        GlobalServerTpsBar.setVisibilityForPlayer(player, true);

        return true;
    }
}
