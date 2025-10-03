package com.toni.wings.server.sound;

import com.toni.wings.WingsMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = WingsMod.ID)
public final class WingsSounds {
    private WingsSounds() {
    }

    public static final DeferredRegister<SoundEvent> REG = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, WingsMod.ID);

    public static final RegistryObject<SoundEvent> ITEM_ARMOR_EQUIP_WINGS = create("item.armor.equip_wings");

    public static final RegistryObject<SoundEvent> ITEM_WINGS_FLYING = create("item.wings.flying");

    private static RegistryObject<SoundEvent> create(String name) {
    return REG.register(name, () -> SoundEvent.createVariableRangeEvent(WingsMod.locate(name)));
    }
}
