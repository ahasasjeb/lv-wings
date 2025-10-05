package com.toni.wings.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.LivingEntity;

public final class ItemAccessor<T extends LivingEntity> {
    private final ImmutableList<ItemPlacing<T>> placings;

    private ItemAccessor(ImmutableList<ItemPlacing<T>> placings) {
        this.placings = placings;
    }

    public Iterable<HandlerSlot> enumerate(T provider) {
        ImmutableList.Builder<HandlerSlot> slots = ImmutableList.builder();
        for (ItemPlacing<T> placing : this.placings) {
            placing.enumerate(provider, slots);
        }
        return slots.build();
    }

    public static <T extends LivingEntity> ItemAccessor<T> none() {
        return new ItemAccessor<>(ImmutableList.of());
    }

    public static <T extends LivingEntity> Builder<T> builder() {
        return new Builder<>();
    }

    public static final class Builder<T extends LivingEntity> {
        private final ImmutableList.Builder<ItemPlacing<T>> placings;

        private Builder() {
            this(ImmutableList.builder());
        }

        private Builder(ImmutableList.Builder<ItemPlacing<T>> placings) {
            this.placings = placings;
        }

        public Builder<T> addPlacing(ItemPlacing<T> placing) {
            this.placings.add(placing);
            return this;
        }

        public ItemAccessor<T> build() {
            return new ItemAccessor<>(this.placings.build());
        }
    }
}
