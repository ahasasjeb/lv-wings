package com.toni.wings.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public interface ItemPlacing<T extends LivingEntity> {
    void enumerate(T provider, ImmutableList.Builder<HandlerSlot> handlers);

    static <T extends LivingEntity> ItemPlacing<T> forArmor(EquipmentSlot slot) {
        return (provider, handlers) -> {
            ResourceHandler<ItemResource> handler = provider.getCapability(Capabilities.Item.ENTITY_AUTOMATION, Direction.EAST);
            if (handler == null) {
                handler = provider.getCapability(Capabilities.Item.ENTITY);
            }
            if (handler != null) {
                handlers.add(HandlerSlot.create(handler, slot.getIndex()));
            }
        };
    }
}
