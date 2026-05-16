package cc.lvjia.wings.server.sound;

import cc.lvjia.wings.WingsMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public final class WingsSounds {
    public static final Supplier<SoundEvent> ITEM_ARMOR_EQUIP_WINGS = create("item.armor.equip_wings");
    public static final Supplier<SoundEvent> ITEM_WINGS_FLYING = create("item.wings.flying");

    private WingsSounds() {
    }

    public static void register() {
    }

    private static Supplier<SoundEvent> create(String name) {
        SoundEvent sound = Registry.register(BuiltInRegistries.SOUND_EVENT, WingsMod.locate(name), SoundEvent.createVariableRangeEvent(WingsMod.locate(name)));
        return () -> sound;
    }
}
