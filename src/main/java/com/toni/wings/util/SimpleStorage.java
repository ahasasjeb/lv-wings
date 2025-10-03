package com.toni.wings.util;

/*public final class SimpleStorage<T> implements Capability.IStorage<T> {
    private final Function<T, CompoundTag> serializer;

    private final Consumer<CompoundTag> deserializer;

    private SimpleStorage(Function<T, CompoundTag> serializer, Consumer<CompoundTag> deserializer) {
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    @Override
    public Tag writeNBT(Capability<T> capability, T instance, Direction side) {
        return this.serializer.apply(instance);
    }

    @Override
    public void readNBT(Capability<T> capability, T instance, Direction side, Tag tag) {
        this.deserializer.accept(tag instanceof CompoundTag ? (CompoundTag) tag : new CompoundTag());
    }

    public static <T> SimpleStorage<T> ofVoid() {
        return new SimpleStorage<>(instance -> null, tag -> {
        });
    }

    public static <T> SimpleStorage<T> of(Function<T, CompoundTag> serializer, Consumer<CompoundTag> deserializer) {
        return new SimpleStorage<>(serializer, deserializer);
    }
}*/
