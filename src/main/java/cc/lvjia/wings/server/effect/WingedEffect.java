package cc.lvjia.wings.server.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class WingedEffect extends MobEffect {
    protected WingedEffect() {
        super(MobEffectCategory.BENEFICIAL, 9947876);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
