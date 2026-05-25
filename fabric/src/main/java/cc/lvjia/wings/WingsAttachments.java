package cc.lvjia.wings;

import cc.lvjia.wings.server.dreamcatcher.InSomniable;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.FlightDefault;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("null")
public final class WingsAttachments {
    public static final @NonNull AttachmentType<@NonNull InSomniable> INSOMNIABLE = Objects.requireNonNull(
            AttachmentRegistry.<@NonNull InSomniable>createDefaulted(WingsMod.locate("insomniable"), InSomniable::new),
            "insomniable attachment");
    private static final @NonNull Codec<@NonNull Flight> FLIGHT_CODEC = Objects.requireNonNull(
            FlightDefault.CODEC.<@NonNull Flight>xmap(
                    WingsAttachments::upcastFlight,
                    WingsAttachments::toDefaultFlight), "flight codec");
    public static final @NonNull AttachmentType<@NonNull Flight> FLIGHT = Objects.requireNonNull(AttachmentRegistry.create(
            WingsMod.locate("flight"),
            builder -> builder
                    .initializer(FlightDefault::new)
                    .persistent(FLIGHT_CODEC)
    ), "flight attachment");
    private static final @NonNull AttachmentType<@NonNull Boolean> FLIGHT_LISTENERS = Objects.requireNonNull(
            AttachmentRegistry.<@NonNull Boolean>create(WingsMod.locate("flight_listeners")), "flight listener marker");

    private WingsAttachments() {
    }

    public static void register() {
        // 触发本类静态初始化，确保持久化 attachment 在玩家存档读取前已注册。
    }

    public static @NonNull Flight getFlight(@NonNull Player player) {
        @Nullable Flight flight = player.getAttached(FLIGHT);
        if (flight == null) {
            flight = new FlightDefault();
            player.setAttached(FLIGHT, flight);
        }
        ensureFlightListeners(player, flight);
        return flight;
    }

    private static @NonNull Flight upcastFlight(@NonNull FlightDefault flight) {
        return flight;
    }

    private static @NonNull FlightDefault toDefaultFlight(@NonNull Flight flight) {
        if (flight instanceof FlightDefault flightDefault) {
            return flightDefault;
        }
        FlightDefault flightDefault = new FlightDefault();
        flightDefault.clone(flight);
        return flightDefault;
    }

    public static boolean hasFlight(@NonNull Player player) {
        return player.hasAttached(FLIGHT);
    }

    public static @NonNull InSomniable getInSomniable(@NonNull Player player) {
        return player.getAttachedOrCreate(INSOMNIABLE);
    }

    private static void ensureFlightListeners(@NonNull Player player, @NonNull Flight flight) {
        if (player.getAttachedOrElse(FLIGHT_LISTENERS, false)) {
            return;
        }
        WingsMod.instance().addFlightListeners(player, flight);
        player.setAttached(FLIGHT_LISTENERS, true);
    }
}
