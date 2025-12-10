package cc.lvjia.wings;

import cc.lvjia.wings.server.dreamcatcher.InSomniable;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.FlightDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

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

    private static final class FlightAttachmentSerializer implements IAttachmentSerializer<Flight> {
        private static final FlightDefault.Serializer SERIALIZER = new FlightDefault.Serializer(FlightDefault::new);

        @Override
        public Flight read(IAttachmentHolder holder, ValueInput input) {
            FlightDefault flight = SERIALIZER.deserialize(input);
            if (holder instanceof Player player) {
                WingsMod.instance().addFlightListeners(player, flight);
            }
            return flight;
        }

        @Override
        public boolean write(Flight attachment, ValueOutput output) {
            if (attachment instanceof FlightDefault flightDefault) {
                SERIALIZER.serialize(flightDefault, output);
                return true;
            }
            throw new IllegalStateException("Unsupported flight implementation: " + attachment.getClass());
        }
    }

    private static final class InSomniableAttachmentSerializer implements IAttachmentSerializer<InSomniable> {
        private static final InSomniable.Serializer SERIALIZER = new InSomniable.Serializer();

        @Override
        public InSomniable read(IAttachmentHolder holder, ValueInput input) {
            return SERIALIZER.deserialize(input);
        }

        @Override
        public boolean write(InSomniable attachment, ValueOutput output) {
            SERIALIZER.serialize(attachment, output);
            return true;
        }
    }
}
