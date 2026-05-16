package cc.lvjia.wings;

import cc.lvjia.wings.server.dreamcatcher.InSomniable;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.FlightDefault;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.player.Player;

public final class WingsAttachments {
    public static final AttachmentType<Flight> FLIGHT = AttachmentRegistry.create(WingsMod.locate("flight"));
    public static final AttachmentType<InSomniable> INSOMNIABLE = AttachmentRegistry.createDefaulted(WingsMod.locate("insomniable"), InSomniable::new);

    private WingsAttachments() {
    }

    public static Flight getFlight(Player player) {
        Flight flight = player.getAttached(FLIGHT);
        if (flight == null) {
            flight = createFlight(player);
            player.setAttached(FLIGHT, flight);
        }
        return flight;
    }

    public static boolean hasFlight(Player player) {
        return player.hasAttached(FLIGHT);
    }

    public static InSomniable getInSomniable(Player player) {
        return player.getAttachedOrCreate(INSOMNIABLE);
    }

    private static Flight createFlight(Player player) {
        FlightDefault flight = new FlightDefault();
        WingsMod.instance().addFlightListeners(player, flight);
        return flight;
    }
}
