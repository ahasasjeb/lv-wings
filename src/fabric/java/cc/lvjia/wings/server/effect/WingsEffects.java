package cc.lvjia.wings.server.effect;

import cc.lvjia.wings.WingsMod;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

/**
 * 药水效果注册表。
 */
@SuppressWarnings("null")
public final class WingsEffects {
    public static final Holder<MobEffect> WINGS = register("wings", new WingedEffect());

    private WingsEffects() {
    }

    public static void register() {
    }

    private static Holder<MobEffect> register(String name, MobEffect effect) {
        MobEffect registered = Registry.register(BuiltInRegistries.MOB_EFFECT, WingsMod.locate(name), effect);
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(registered);
    }
}
