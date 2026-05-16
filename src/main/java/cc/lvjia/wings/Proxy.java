package cc.lvjia.wings;

import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.net.Message;
import cc.lvjia.wings.server.net.Network;
import cc.lvjia.wings.server.net.clientbound.MessageSyncFlight;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class Proxy {
    protected final Network network = new Network();

    public void init() {
        this.network.register();
    }

    @SuppressWarnings("deprecation")
    public void addFlightListeners(Player player, Flight instance) {
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
                    () -> this.network.sendToPlayer(new MessageSyncFlight(player, instance), serverPlayer),
                    p -> this.network.sendToPlayer(new MessageSyncFlight(player, instance), p),
                    () -> this.network.sendToAllTracking(new MessageSyncFlight(player, instance), serverPlayer)
            );
            instance.registerSyncListener(players -> players.notify(notifier));
            // 先把当前权威状态同步给追踪者，避免新加入视角时看到旧值
            instance.sync(Flight.PlayerSet.ofOthers());
        }
    }

    public void invalidateFlightView(Player player) {
    }

    public void sendToServer(Message message) {
        throw new UnsupportedOperationException("sendToServer is client-only");
    }
}
