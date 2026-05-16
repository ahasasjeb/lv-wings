package cc.lvjia.wings;

import cc.lvjia.wings.server.dreamcatcher.InSomniable;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.FlightDefault;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.player.Player;

public final class WingsAttachments {
    public static final AttachmentType<InSomniable> INSOMNIABLE = AttachmentRegistry.createDefaulted(WingsMod.locate("insomniable"), InSomniable::new);
    private static final Codec<Flight> FLIGHT_CODEC = FlightDefault.CODEC.xmap(
            flight -> flight,
            flight -> {
                if (flight instanceof FlightDefault flightDefault) {
                    return flightDefault;
                }
                FlightDefault flightDefault = new FlightDefault();
                flightDefault.clone(flight);
                return flightDefault;
            }
    );
    public static final AttachmentType<Flight> FLIGHT = AttachmentRegistry.create(
            WingsMod.locate("flight"),
            builder -> builder
                    .initializer(FlightDefault::new)
                    .persistent(FLIGHT_CODEC)
    );
    private static final AttachmentType<Boolean> FLIGHT_LISTENERS = AttachmentRegistry.create(WingsMod.locate("flight_listeners"));

    private WingsAttachments() {
    }

    public static void register() {
        // 触发本类静态初始化，确保持久化 attachment 在玩家存档读取前已注册。
    }

    public static Flight getFlight(Player player) {
        Flight flight = player.getAttached(FLIGHT);
        if (flight == null) {
            flight = new FlightDefault();
            player.setAttached(FLIGHT, flight);
        }
        ensureFlightListeners(player, flight);
        return flight;
    }

    public static boolean hasFlight(Player player) {
        return player.hasAttached(FLIGHT);
    }

    public static InSomniable getInSomniable(Player player) {
        return player.getAttachedOrCreate(INSOMNIABLE);
    }

    private static void ensureFlightListeners(Player player, Flight flight) {
        if (Boolean.TRUE.equals(player.getAttached(FLIGHT_LISTENERS))) {
            return;
        }
        WingsMod.instance().addFlightListeners(player, flight);
        player.setAttached(FLIGHT_LISTENERS, true);
    }
}
