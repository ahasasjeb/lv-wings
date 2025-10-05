package com.toni.wings.server.effect;

import com.toni.wings.WingsMod;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

public final class WingsEffects {
    private WingsEffects() {
    }

    public static final DeferredRegister<MobEffect> REG = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, WingsMod.ID);

    public static final RegistryObject<MobEffect> WINGS = REG.register("wings", () -> new WingedEffect(0x97cae4));
}
