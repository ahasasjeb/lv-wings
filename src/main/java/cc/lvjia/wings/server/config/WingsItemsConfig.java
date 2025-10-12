package cc.lvjia.wings.server.config;

import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.server.item.WingSettings;
import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.stream.Stream;

public final class WingsItemsConfig {
    public static final ModConfigSpec SPEC;
    public static final ConfigWingSettings ANGEL;
    public static final ConfigWingSettings PARROT;
    public static final ConfigWingSettings SLIME;
    public static final ConfigWingSettings BLUE_BUTTERFLY;
    public static final ConfigWingSettings MONARCH_BUTTERFLY;
    public static final ConfigWingSettings FIRE;
    public static final ConfigWingSettings BAT;
    public static final ConfigWingSettings FAIRY;
    public static final ConfigWingSettings EVIL;
    public static final ConfigWingSettings DRAGON;
    public static final ConfigWingSettings LVJIA_SUPER;

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

        builder.pop();
        SPEC = builder.build();
    }

    private WingsItemsConfig() {
    }

    public static ImmutableMap<ResourceLocation, WingSettings> createWingAttributes() {
        return Stream.of(ANGEL, PARROT, SLIME, BLUE_BUTTERFLY, MONARCH_BUTTERFLY, FIRE, BAT, FAIRY, EVIL, DRAGON, LVJIA_SUPER)
                .collect(ImmutableMap.toImmutableMap(ConfigWingSettings::getKey, ConfigWingSettings::toImmutable));
    }

    public static void validate() {
        Stream.of(ANGEL, PARROT, SLIME, BLUE_BUTTERFLY, MONARCH_BUTTERFLY, FIRE, BAT, FAIRY, EVIL, DRAGON, LVJIA_SUPER)
                .forEach(ConfigWingSettings::validate);
    }
}
