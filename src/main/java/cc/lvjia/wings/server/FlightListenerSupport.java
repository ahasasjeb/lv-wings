package cc.lvjia.wings.server;

import cc.lvjia.wings.server.flight.Flight;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class FlightListenerSupport {
    private FlightListenerSupport() {
    }

    public static void addFlightListeners(Player player, Flight instance, Sync sync) {
        if (player instanceof ServerPlayer serverPlayer) {
            instance.registerFlyingListener(isFlying -> {
                boolean hasVanillaFlight = player.getAbilities().instabuild || player.isSpectator();
                player.getAbilities().mayfly = isFlying || hasVanillaFlight;
                if (isFlying || !hasVanillaFlight) {
                    player.getAbilities().flying = false;
                }
                serverPlayer.onUpdateAbilities();
            });
            instance.registerFlyingListener(isFlying -> {
                if (isFlying) {
                    player.removeVehicle();
                }
            });
            Flight.Notifier notifier = Flight.Notifier.of(
                    () -> sync.sendToPlayer(player, instance, serverPlayer),
                    target -> sync.sendToPlayer(player, instance, target),
                    () -> sync.sendToAllTracking(player, instance, serverPlayer)
            );
            instance.registerSyncListener(players -> players.notify(notifier));
            // Keep newly tracking clients from seeing a stale flight snapshot.
            instance.sync(Flight.PlayerSet.ofOthers());
        }
    }

    public interface Sync {
        void sendToPlayer(Player player, Flight flight, ServerPlayer target);

        void sendToAllTracking(Player player, Flight flight, ServerPlayer trackedEntity);
    }
}
