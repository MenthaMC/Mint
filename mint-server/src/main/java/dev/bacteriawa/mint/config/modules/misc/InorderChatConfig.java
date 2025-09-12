package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.annotation.Config;
import dev.bacteriawa.mint.config.annotation.ConfigField;

@Config(name = "mojang_out_of_order_chat_check", category = ConfigCategory.misc)
public class InorderChatConfig {
    @ConfigField
    public static boolean enabled = true;
}
