package cc.lvjia.wings.client;

import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.Flights;
import cc.lvjia.wings.server.item.BatBloodBottleItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class ClientFlightInputActions {
    private ClientFlightInputActions() {
    }

    public static void toggleLocalFlight() {
        Player player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator()) {
            return;
        }
        Flights.get(player).toggleIsFlying(Flight.PlayerSet.ofOthers());
        Flights.ifPlayer(player, (p, flight) -> {
            if (flight.getWing().equals(WingsMod.WINGLESS) && !flight.isFlying()) {
                BatBloodBottleItem.removeWings(player);
            }
        });
    }
}
