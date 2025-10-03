package com.toni.wings.server.effect;

import com.toni.wings.WingsMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class WingsEffects {
    private WingsEffects() {
    }

    public static final DeferredRegister<MobEffect> REG = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, WingsMod.ID);

    public static final RegistryObject<MobEffect> WINGS = REG.register("wings", () -> new WingedEffect(0x97cae4));
}
