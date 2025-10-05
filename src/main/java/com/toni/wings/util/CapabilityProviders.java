package com.toni.wings.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.capabilities.BaseCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class CapabilityProviders {
    private CapabilityProviders() {
    }

    public static CapabilityProvider empty() {
        return EmptyProvider.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public static <T> NonSerializingSingleBuilder<T> emptyBuilder() {
        return (EmptySingleBuilder<T>) EmptySingleBuilder.INSTANCE;
    }

    public static <T> NonSerializingSingleBuilder<T> builder(BaseCapability<? super T, ?> capability, T instance) {
        return new NonSerializingSingleBuilderImpl<>(capability, instance);
    }

    public static CompositeBuilder builder() {
        return new CompositeBuilderImpl();
    }

    @FunctionalInterface
    public interface CapabilityProvider {
        <T, C extends @Nullable Object> @Nullable T getCapability(@NotNull BaseCapability<T, C> capability, @Nullable C context);
    }

    public interface CompositeBuilder {
        CompositeBuilder add(CapabilityProvider provider);

        CapabilityProvider build();
    }

    private static final class CompositeBuilderImpl implements CompositeBuilder {
        private final ImmutableList.Builder<CapabilityProvider> providers;

        private CompositeBuilderImpl() {
            this(ImmutableList.builder());
        }

        private CompositeBuilderImpl(ImmutableList.Builder<CapabilityProvider> providers) {
            this.providers = providers;
        }

        @Override
        public CompositeBuilder add(CapabilityProvider provider) {
            this.providers.add(provider);
            return this;
        }

        @Override
        public CapabilityProvider build() {
            ImmutableList<CapabilityProvider> providers = this.providers.build();
            return switch (providers.size()) {
                case 0 -> empty();
                case 1 -> providers.get(0);
                default -> new CompositeProvider(providers);
            };
        }
    }

    private static final class CompositeProvider implements CapabilityProvider {
        private final ImmutableList<CapabilityProvider> providers;

        private CompositeProvider(ImmutableList<CapabilityProvider> providers) {
            this.providers = providers;
        }

        @Override
        public <T, C extends @Nullable Object> @Nullable T getCapability(@NotNull BaseCapability<T, C> capability, @Nullable C context) {
            for (CapabilityProvider provider : this.providers) {
                T value = provider.getCapability(capability, context);
                if (value != null) {
                    return value;
                }
            }
            return null;
        }
    }

    private static final class EmptySingleBuilder<T> implements NonSerializingSingleBuilder<T> {
        private static final EmptySingleBuilder<?> INSTANCE = new EmptySingleBuilder<>();

        @Override
        public <N extends Tag> SingleBuilder<T> serializedBy(NBTSerializer<T, N> serializer) {
            return this;
        }

        @Override
        public SingleBuilder<T> peek(Consumer<T> consumer) {
            return this;
        }

        @Override
        public CapabilityProvider build() {
            return empty();
        }
    }

    private static final class EmptyProvider implements CapabilityProvider {
        private static final EmptyProvider INSTANCE = new EmptyProvider();

        @Override
        public <T, C extends @Nullable Object> @Nullable T getCapability(@NotNull BaseCapability<T, C> capability, @Nullable C context) {
            return null;
        }
    }

    private static abstract class SingleProvider<T> implements CapabilityProvider {
        final BaseCapability<?, ?> capability;

        T instance;

        private SingleProvider(BaseCapability<? super T, ?> capability, T instance) {
            this.capability = capability;
            this.instance = instance;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <X, C extends @Nullable Object> @Nullable X getCapability(@NotNull BaseCapability<X, C> capability, @Nullable C context) {
            if (this.capability == capability) {
                return (X) this.instance;
            }
            return null;
        }
    }

    private static final class SimpleSingleProvider<T> extends SingleProvider<T> {
        private SimpleSingleProvider(BaseCapability<? super T, ?> capability, T instance) {
            super(capability, instance);
        }
    }

    public interface NBTBackedProvider<N extends Tag> extends CapabilityProvider {
        N serializeNBT(HolderLookup.Provider registryAccess);

        void deserializeNBT(HolderLookup.Provider registryAccess, N tag);
    }

    private static final class SerializingSingleProvider<T, N extends Tag> extends SingleProvider<T> implements NBTBackedProvider<N> {
        final NBTSerializer<T, N> serializer;

        private SerializingSingleProvider(BaseCapability<? super T, ?> capability, T instance, NBTSerializer<T, N> serializer) {
            super(capability, instance);
            this.serializer = serializer;
        }

        @Override
        public N serializeNBT(HolderLookup.Provider registryAccess) {
            return this.serializer.serialize(this.instance);
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider registryAccess, N compound) {
            this.instance = this.serializer.deserialize(compound);
        }
    }

    private static abstract class AbstractSingleBuilder<T> implements SingleBuilder<T> {
        final BaseCapability<? super T, ?> capability;

        final T instance;

        AbstractSingleBuilder(BaseCapability<? super T, ?> capability, T instance) {
            this.capability = capability;
            this.instance = instance;
        }
    }

    public interface SingleBuilder<T> {
        SingleBuilder<T> peek(Consumer<T> consumer);

        CapabilityProvider build();
    }

    public interface NonSerializingSingleBuilder<T> extends SingleBuilder<T> {
        <N extends Tag> SingleBuilder<T> serializedBy(NBTSerializer<T, N> serializer);
    }

    private static final class NonSerializingSingleBuilderImpl<T> extends AbstractSingleBuilder<T> implements NonSerializingSingleBuilder<T> {
        private NonSerializingSingleBuilderImpl(BaseCapability<? super T, ?> capability, T instance) {
            super(capability, instance);
        }

        @Override
        public <N extends Tag> SerializingSingleBuilderImpl<T, N> serializedBy(NBTSerializer<T, N> serializer) {
            return new SerializingSingleBuilderImpl<>(this.capability, this.instance, serializer);
        }

        @Override
        public NonSerializingSingleBuilder<T> peek(Consumer<T> consumer) {
            consumer.accept(this.instance);
            return this;
        }

        @Override
        public CapabilityProvider build() {
            return new SimpleSingleProvider<>(this.capability, this.instance);
        }
    }

    private static final class SerializingSingleBuilderImpl<T, N extends Tag> extends AbstractSingleBuilder<T> {
        private final NBTSerializer<T, N> serializer;

        private SerializingSingleBuilderImpl(BaseCapability<? super T, ?> capability, T instance, NBTSerializer<T, N> serializer) {
            super(capability, instance);
            this.serializer = serializer;
        }

        @Override
        public SerializingSingleBuilderImpl<T, N> peek(Consumer<T> consumer) {
            consumer.accept(this.instance);
            return this;
        }

        @Override
        public CapabilityProvider build() {
            return new SerializingSingleProvider<>(this.capability, this.instance, this.serializer);
        }
    }
}
