package dev.bacteriawa.mint.commands.subcommand;

import dev.bacteriawa.mint.commands.MintSubCommand;
import dev.bacteriawa.mint.config.MintConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ConfigReloadCommand extends MintSubCommand {
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        MintConfig.reloadConfig();
        sender.sendMessage(Component.text("The configuration file has been reloaded.").color(TextColor.color(0, 255, 0)));
        return true;
    }
}
