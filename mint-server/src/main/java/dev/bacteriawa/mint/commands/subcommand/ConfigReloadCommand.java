package dev.bacteriawa.mint.commands.subcommand;

import dev.bacteriawa.mint.commands.MintSubCommand;
import dev.bacteriawa.mint.config.MintConfig;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ConfigReloadCommand extends MintSubCommand {
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        MintConfig.reloadConfig();
        return true;
    }
}
