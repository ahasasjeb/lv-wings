package cc.lvjia.wings.client;

import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.Flights;
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
    }
}
