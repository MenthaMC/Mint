package dev.bacteriawa.mint.config.modules.misc;

import dev.bacteriawa.mint.config.ConfigCategory;
import dev.bacteriawa.mint.config.ConfigField;
import dev.bacteriawa.mint.config.Configuration;

@Configuration(name = "verify_publickey_only_in_online_mode", type = ConfigCategory.misc)
public class PublickeyVerifyConfig {
    @ConfigField(comment = "Verify public key only in online mode", commentZh = "仅在在线模式下验证公钥")
    public static boolean enabled = false;
}
