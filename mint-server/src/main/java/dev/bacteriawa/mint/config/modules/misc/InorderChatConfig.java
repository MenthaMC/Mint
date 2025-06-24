package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "mojang_out_of_order_chat_check", type = ConfigCategory.misc)
public class InorderChatConfig {
    @ConfigField
    public static boolean enabled = true;
}
