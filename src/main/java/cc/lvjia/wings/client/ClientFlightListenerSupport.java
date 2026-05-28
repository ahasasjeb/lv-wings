package cc.lvjia.wings.client;

import cc.lvjia.wings.server.flight.Flight;
import net.minecraft.world.entity.player.Player;

public final class ClientFlightListenerSupport {
    private ClientFlightListenerSupport() {
    }

    public static void addLocalFlightSyncListener(Player player, Flight flight, FlyingStateSender sender) {
        if (!player.isLocalPlayer()) {
            return;
        }
        Flight.Notifier notifier = Flight.Notifier.of(
                () -> {
                },
                target -> {
                },
                () -> sender.send(flight.isFlying())
        );
        flight.registerSyncListener(players -> players.notify(notifier));
    }

    @FunctionalInterface
    public interface FlyingStateSender {
        void send(boolean isFlying);
    }
}
