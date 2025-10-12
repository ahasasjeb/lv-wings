package cc.lvjia.wings.server.sound;

import cc.lvjia.wings.WingsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WingsSounds {
    public static final DeferredRegister<SoundEvent> REG = DeferredRegister.create(Registries.SOUND_EVENT, WingsMod.ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_ARMOR_EQUIP_WINGS = create("item.armor.equip_wings");
    public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_WINGS_FLYING = create("item.wings.flying");

    private WingsSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> create(String name) {
        return REG.register(name, () -> SoundEvent.createVariableRangeEvent(WingsMod.locate(name)));
    }
}
