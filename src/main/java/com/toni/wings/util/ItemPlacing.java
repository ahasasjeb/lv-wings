package com.toni.wings.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface ItemPlacing<T extends ICapabilityProvider> {
    void enumerate(T provider, ImmutableList.Builder<HandlerSlot> handlers);

    static <T extends LivingEntity> ItemPlacing<T> forArmor(EquipmentSlot slot) {
        return (provider, handlers) -> provider.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.EAST)
            .ifPresent(handler -> handlers.add(HandlerSlot.create(handler, slot.getIndex())));
    }
}
