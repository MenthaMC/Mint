package dev.bacteriawa.mint.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MintCommand extends BukkitCommand {
    private static final Component PERMISSION_MSG = Component.text("No permission to execute this command!").color(NamedTextColor.RED);
    private static final Component SUB_COMMAND_NOT_EXIST_MSG = Component.text("Not exist command: ").color(NamedTextColor.RED);
    private static final Map<String, MintSubCommand> subcommands = new ConcurrentHashMap<>();
    public static final Permission MINT_ADMIN_PERMISSION = new Permission("mint.command.admin", PermissionDefault.OP);
    public static final Permission MINT_USER_PERMISSION = new Permission("mint.command.user", PermissionDefault.TRUE);

    public MintCommand() {
        super("mint");
        this.setUsage("/mint <subcommand>");
    }

    public static void registerSubCommand(MintSubCommand subcommand) {
        subcommands.put(subcommand.getName(), subcommand);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: " + this.getUsage()));
        }

        if (args.length > 0) {
            String subCommand = args[0];

            if (subcommands.containsKey(subCommand)) {
                MintSubCommand mintSubCommand = subcommands.get(subCommand);
                if (sender.hasPermission(mintSubCommand.permission)) {
                    return mintSubCommand.execute(sender, subCommand, Arrays.copyOfRange(args, 1, args.length));
                } else {
                    sender.sendMessage(PERMISSION_MSG);
                }
            } else {
                sender.sendMessage(SUB_COMMAND_NOT_EXIST_MSG.append(Component.text(subCommand).color(NamedTextColor.RED)));
                return true;
            }
        }

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length == 1){
            return subcommands.keySet().stream().filter(i -> sender.hasPermission(subcommands.get(i).permission)).toList();
        }

        if (args.length > 1 && subcommands.containsKey(args[0])) {
            MintSubCommand mintSubCommand = subcommands.get(args[0]);
            if (sender.hasPermission(mintSubCommand.permission)) {
                return mintSubCommand.tabComplete(sender, alias, Arrays.copyOfRange(args, 1, args.length));
            } else {
                return List.of();
            }
        }

        return List.of();
    }
}
