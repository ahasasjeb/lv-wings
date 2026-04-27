package com.toni.wings.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import java.util.Objects;

public interface ItemPlacing<T extends ICapabilityProvider> {
    void enumerate(T provider, ImmutableList.Builder<HandlerSlot> handlers);

    @Nonnull
    static <T extends LivingEntity> ItemPlacing<T> forArmor(@Nonnull EquipmentSlot slot) {
        return (provider, handlers) -> provider.getCapability(
                Objects.requireNonNull(ForgeCapabilities.ITEM_HANDLER, "Item handler capability cannot be null"),
                Objects.requireNonNull(Direction.EAST, "Direction cannot be null")
            )
            .ifPresent(handler -> handlers.add(HandlerSlot.create(
                Objects.requireNonNull(handler, "Item handler cannot be null"),
                slot.getIndex()
            )));
    }
}
