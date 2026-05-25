package cc.lvjia.wings.server.config;

import cc.lvjia.wings.WingsMod;
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

        ANGEL = new ConfigWingSettings(WingsMod.Names.ANGEL, builder);
        PARROT = new ConfigWingSettings(WingsMod.Names.PARROT, builder);
        SLIME = new ConfigWingSettings(WingsMod.Names.SLIME, builder);
        BLUE_BUTTERFLY = new ConfigWingSettings(WingsMod.Names.BLUE_BUTTERFLY, builder);
        MONARCH_BUTTERFLY = new ConfigWingSettings(WingsMod.Names.MONARCH_BUTTERFLY, builder);
        FIRE = new ConfigWingSettings(WingsMod.Names.FIRE, builder);
        BAT = new ConfigWingSettings(WingsMod.Names.BAT, builder);
        FAIRY = new ConfigWingSettings(WingsMod.Names.FAIRY, builder);
        EVIL = new ConfigWingSettings(WingsMod.Names.EVIL, builder);
        DRAGON = new ConfigWingSettings(WingsMod.Names.DRAGON, builder);
        LVJIA_SUPER = new ConfigWingSettings(WingsMod.Names.LVJIA_SUPER, builder);
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
