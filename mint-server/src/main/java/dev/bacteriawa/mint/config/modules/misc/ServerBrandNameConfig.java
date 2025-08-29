package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "server-brand-name", type = ConfigCategory.misc)
public class ServerBrandNameConfig {
    @ConfigField(comment = "Server brand name displayed to clients", commentZh = "向客户端显示的服务器品牌名称")
    public static String serverModName= "Mint";
}
