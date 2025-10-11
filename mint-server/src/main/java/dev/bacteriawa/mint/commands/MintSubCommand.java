package dev.bacteriawa.mint.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class MintSubCommand extends BukkitCommand {
    final Permission permission;

    public MintSubCommand(@NotNull String name, @NotNull String description, Permission permission) {
        super(name, description, "/mint " + name, List.of());
        this.permission = permission;
        this.setPermission(this.permission.getName());
    }

    public MintSubCommand(@NotNull String name, @NotNull Permission permission) {
        this(name, "Unknown Command Description.", permission);
    }

    public MintSubCommand(@NotNull String name) {
        this(name, MintCommand.MINT_ADMIN_PERMISSION);
    }

    public abstract boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String @NotNull [] args);
}
