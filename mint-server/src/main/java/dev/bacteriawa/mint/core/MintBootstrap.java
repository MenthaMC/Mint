package dev.bacteriawa.mint.core;

import dev.bacteriawa.mint.commands.MintCommand;
import dev.bacteriawa.mint.utils.ServerI18nUtil;
import joptsimple.OptionSet;
import net.minecraft.server.Main;
import org.bukkit.Bukkit;

public class MintBootstrap {
    public static void bootstrap(OptionSet options) throws Exception {
        dev.bacteriawa.mint.config.MintConfiguration.loadAllConfigs(); // Mint - Blank Configuration System.
        dev.bacteriawa.mint.utils.ServerI18nUtil.init(); // Mint - I18n support

        Main.main(options); // Minecraft Main class
    }

    public static void setup() throws Exception {
        /* Mint start - Mint Command System. */
        Bukkit.getPluginManager().addPermission(MintCommand.MINT_USER_PERMISSION);
        Bukkit.getPluginManager().addPermission(MintCommand.MINT_ADMIN_PERMISSION);
        Bukkit.getCommandMap().register("mint", new MintCommand());
        /* Mint end - Mint Command System. */

        ServerI18nUtil.preInit(); // Mint - I18n support
        dev.bacteriawa.mint.config.MintConfiguration.setupAllConfigs(); // Mint - Blank Configuration System.
    }
}
