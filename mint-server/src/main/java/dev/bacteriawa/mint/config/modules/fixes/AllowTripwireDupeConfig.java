package dev.bacteriawa.mint.config.modules.fixes;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.bacteriawa.mint.config.annotation.Configuration;
import dev.bacteriawa.mint.config.annotation.Configurations;
import dev.bacteriawa.mint.enums.EnumTripwireBehavior;

import static dev.bacteriawa.mint.config.ConfigurationType.fixes;

@Configurations(name = "tripwire_dupe", type =  fixes)
public class AllowTripwireDupeConfig {
    @Configuration
    public static boolean enabled = false;

    @Configuration
    public static EnumTripwireBehavior behaviorMode = EnumTripwireBehavior.VANILLA21;

    public static void loaded(CommentedFileConfig config) {
        System.out.println(behaviorMode);
    }
}
