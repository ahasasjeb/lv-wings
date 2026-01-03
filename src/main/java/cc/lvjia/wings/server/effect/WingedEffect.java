package cc.lvjia.wings.server.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 翅膀效果（用于标记/触发与飞行相关的能力）。
 */
public class WingedEffect extends MobEffect {
    protected WingedEffect() {
        // 颜色值用于客户端 UI 显示。
        super(MobEffectCategory.BENEFICIAL, 9947876);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
