package dev.bacteriawa.mint.commands;

import dev.bacteriawa.mint.commands.subcommand.ConfigReloadCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MintCommand extends BukkitCommand {
    private final Map<String, MintSubCommand> subcommands = new HashMap<>();

    public MintCommand() {
        super("mint");
        this.setUsage("/commands <usage>");

        subcommands.put("reload", new ConfigReloadCommand());
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!testPermission(sender) || !sender.isOp()) {
            sender.sendMessage(Component
                    .text("No permission to execute this command!")
                    .color(NamedTextColor.RED)
            );
        }

        if (args.length < 1) {
            sender.sendMessage(Component
                    .text("Usage: /mint <commands>")
                    .color(NamedTextColor.RED)
            );
        }

        if (args.length >= 1) {
            String subCommand = args[0];

            if (subcommands.containsKey(subCommand)) {
                return subcommands.get(subCommand).execute(
                        sender, subCommand,
                        Arrays.copyOfRange(args, 1, args.length)
                );
            } else {
                sender.sendMessage(Component
                        .text("Not exist command: " + subCommand)
                        .color(NamedTextColor.RED)
                );
            }
        }

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length == 1){
            return subcommands.keySet().stream().toList();
        }

        return List.of();
    }
}

