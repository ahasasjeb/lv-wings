package com.toni.wings.util;

import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class HandlerSlot {
    private final ResourceHandler<ItemResource> handler;

    private final int slot;

    private HandlerSlot(ResourceHandler<ItemResource> handler, int slot) {
        this.handler = handler;
        this.slot = slot;
    }

    public ItemStack get() {
        return ItemUtil.getStack(this.handler, this.slot);
    }

    @SuppressWarnings("null")
    public ItemStack insert(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemResource resource = ItemResource.of(stack);
        if (resource.isEmpty()) {
            return stack;
        }

    return ItemUtil.insertItemReturnRemaining(this.handler, this.slot, stack, false, (TransactionContext) null);
    }

    @SuppressWarnings("null")
    public ItemStack extract(int amount) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }

        ItemResource resource = this.handler.getResource(this.slot);
        if (resource.isEmpty()) {
            return ItemStack.EMPTY;
        }

    try (var tx = Transaction.open((TransactionContext) null)) {
            int extracted = this.handler.extract(this.slot, resource, amount, tx);
            if (extracted <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack result = resource.toStack(extracted);
            tx.commit();
            return result;
        }
    }

    public int getLimit() {
        return this.handler.getCapacityAsInt(this.slot, ItemResource.EMPTY);
    }

    public static HandlerSlot create(ResourceHandler<ItemResource> handler, int slot) {
        return new HandlerSlot(Objects.requireNonNull(handler, "handler"), slot);
    }
}
