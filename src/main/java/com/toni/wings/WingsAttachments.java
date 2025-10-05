package com.toni.wings;

import com.toni.wings.server.dreamcatcher.InSomniable;
import com.toni.wings.server.flight.Flight;
import com.toni.wings.server.flight.FlightDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import javax.annotation.Nonnull;

public final class WingsAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, WingsMod.ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Flight>> FLIGHT = ATTACHMENT_TYPES.register("flight", () ->
        AttachmentType.builder(WingsAttachments::createFlight)
            .serialize(new FlightAttachmentSerializer())
            .copyOnDeath()
            .build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<InSomniable>> INSOMNIABLE = ATTACHMENT_TYPES.register("insomniable", () ->
        AttachmentType.builder(holder -> new InSomniable())
            .serialize(new InSomniableAttachmentSerializer())
            .copyOnDeath()
            .build()
    );

    private WingsAttachments() {
    }

    static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }

    private static Flight createFlight(IAttachmentHolder holder) {
        if (holder instanceof Player player) {
            FlightDefault flight = new FlightDefault();
            WingsMod.instance().addFlightListeners(player, flight);
            return flight;
        }
        throw new IllegalStateException("Flight attachment can only be applied to players");
    }

    private static final class FlightAttachmentSerializer implements IAttachmentSerializer<CompoundTag, Flight> {
        private static final FlightDefault.Serializer SERIALIZER = new FlightDefault.Serializer(FlightDefault::new);

        @Override
    public Flight read(@Nonnull IAttachmentHolder holder, @Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider provider) {
            FlightDefault flight = SERIALIZER.deserialize(tag);
            if (holder instanceof Player player) {
                WingsMod.instance().addFlightListeners(player, flight);
            }
            return flight;
        }

        @Override
    public CompoundTag write(@Nonnull Flight attachment, @Nonnull HolderLookup.Provider provider) {
            if (attachment instanceof FlightDefault flightDefault) {
                return SERIALIZER.serialize(flightDefault);
            }
            throw new IllegalStateException("Unsupported flight implementation: " + attachment.getClass());
        }
    }

    private static final class InSomniableAttachmentSerializer implements IAttachmentSerializer<CompoundTag, InSomniable> {
        private static final InSomniable.Serializer SERIALIZER = new InSomniable.Serializer();

        @Override
    public InSomniable read(@Nonnull IAttachmentHolder holder, @Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider provider) {
            return SERIALIZER.deserialize(tag);
        }

        @Override
    public CompoundTag write(@Nonnull InSomniable attachment, @Nonnull HolderLookup.Provider provider) {
            return SERIALIZER.serialize(attachment);
        }
    }
}
