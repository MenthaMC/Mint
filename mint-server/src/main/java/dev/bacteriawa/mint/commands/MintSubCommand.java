package dev.bacteriawa.mint.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class MintSubCommand extends BukkitCommand {
    public MintSubCommand(@NotNull String name, @NotNull String description, @NotNull String usageMessage, @NotNull List<String> aliases) {
        super(name, description, usageMessage, aliases);
    }

    public MintSubCommand(@NotNull String name) {
        super(name);
    }

    public abstract boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String @NotNull [] args);
}
