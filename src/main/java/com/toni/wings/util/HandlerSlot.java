package com.toni.wings.util;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public final class HandlerSlot {
    private final IItemHandler handler;

    private final int slot;

    private HandlerSlot(IItemHandler handler, int slot) {
        this.handler = handler;
        this.slot = slot;
    }

    public ItemStack get() {
        return this.handler.getStackInSlot(this.slot);
    }

    public ItemStack insert(ItemStack stack) {
        return this.handler.insertItem(this.slot, stack, false);
    }

    public ItemStack extract(int amount) {
        return this.handler.extractItem(this.slot, amount, false);
    }

    public int getLimit() {
        return this.handler.getSlotLimit(this.slot);
    }

    public static HandlerSlot create(IItemHandler handler, int slot) {
        return new HandlerSlot(handler, slot);
    }
}
