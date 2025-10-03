package com.toni.wings.server.effect;

import com.toni.wings.server.item.WingsItems;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WingedEffect extends MobEffect {
    protected WingedEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        List<ItemStack> ret = new ArrayList<>();
        ret.add(new ItemStack(WingsItems.BAT_BLOOD_BOTTLE.get()));
        return ret;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
