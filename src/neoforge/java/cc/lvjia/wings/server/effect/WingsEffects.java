package cc.lvjia.wings.server.effect;

import cc.lvjia.wings.WingsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 药水效果注册表。
 */
public final class WingsEffects {
    public static final DeferredRegister<MobEffect> REG = DeferredRegister.create(Registries.MOB_EFFECT, WingsMod.ID);
    public static final DeferredHolder<MobEffect, MobEffect> WINGS = REG.register("wings", () -> new WingedEffect());

    private WingsEffects() {
    }
}
