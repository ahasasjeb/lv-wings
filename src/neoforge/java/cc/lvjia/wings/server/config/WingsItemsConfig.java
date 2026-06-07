package cc.lvjia.wings.server.config;

import cc.lvjia.wings.WingsCore;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jspecify.annotations.NonNull;

import java.util.List;

public final class WingsItemsConfig {
    public static final @NonNull ModConfigSpec SPEC;
    public static final @NonNull ConfigWingSettings ANGEL;
    public static final @NonNull ConfigWingSettings PARROT;
    public static final @NonNull ConfigWingSettings SLIME;
    public static final @NonNull ConfigWingSettings BLUE_BUTTERFLY;
    public static final @NonNull ConfigWingSettings MONARCH_BUTTERFLY;
    public static final @NonNull ConfigWingSettings FIRE;
    public static final @NonNull ConfigWingSettings BAT;
    public static final @NonNull ConfigWingSettings FAIRY;
    public static final @NonNull ConfigWingSettings EVIL;
    public static final @NonNull ConfigWingSettings DRAGON;
    public static final @NonNull ConfigWingSettings LVJIA_SUPER;
    private static final @NonNull List<@NonNull ConfigWingSettings> ALL;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Wing attribute configuration values").push("wings");

        ANGEL = new ConfigWingSettings(WingsCore.Names.ANGEL, builder);
        PARROT = new ConfigWingSettings(WingsCore.Names.PARROT, builder);
        SLIME = new ConfigWingSettings(WingsCore.Names.SLIME, builder);
        BLUE_BUTTERFLY = new ConfigWingSettings(WingsCore.Names.BLUE_BUTTERFLY, builder);
        MONARCH_BUTTERFLY = new ConfigWingSettings(WingsCore.Names.MONARCH_BUTTERFLY, builder);
        FIRE = new ConfigWingSettings(WingsCore.Names.FIRE, builder);
        BAT = new ConfigWingSettings(WingsCore.Names.BAT, builder);
        FAIRY = new ConfigWingSettings(WingsCore.Names.FAIRY, builder);
        EVIL = new ConfigWingSettings(WingsCore.Names.EVIL, builder);
        DRAGON = new ConfigWingSettings(WingsCore.Names.DRAGON, builder);
        LVJIA_SUPER = new ConfigWingSettings(WingsCore.Names.LVJIA_SUPER, builder);
        ALL = List.of(ANGEL, PARROT, SLIME, BLUE_BUTTERFLY, MONARCH_BUTTERFLY, FIRE, BAT, FAIRY, EVIL, DRAGON, LVJIA_SUPER);

        builder.pop();
        SPEC = builder.build();
    }

    private WingsItemsConfig() {
    }

    public static void validate() {
        ALL.forEach(ConfigWingSettings::validate);
    }
}
