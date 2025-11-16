package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigurationType;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;

@Configurations(name = "mojang_out_of_order_chat_check", type = ConfigurationType.misc)
public class InorderChatConfig {
    @Configuration
    public static boolean enabled = true;
}
