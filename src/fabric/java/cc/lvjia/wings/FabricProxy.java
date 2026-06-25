package cc.lvjia.wings;

import cc.lvjia.wings.server.FlightListenerSupport;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.net.Message;
import cc.lvjia.wings.server.net.Network;
import cc.lvjia.wings.server.net.clientbound.MessageSyncFlight;
import net.minecraft.world.entity.player.Player;

// Fabric 平台代理：网络注册 + 飞行监听器绑定
public class FabricProxy {
    protected final Network network = new Network();

    public void init() {
        this.network.register();
    }

    // 为玩家 Flight 注册同步监听器，使用 Fabric API 发送网络包
    public void addFlightListeners(Player player, Flight instance) {
        FlightListenerSupport.addFlightListeners(player, instance, new FlightListenerSupport.Sync() {
            @Override
            public void sendToPlayer(Player source, Flight flight, net.minecraft.server.level.ServerPlayer target) {
                network.sendToPlayer(new MessageSyncFlight(source, flight), target);
            }

            @Override
            public void sendToAllTracking(Player source, Flight flight, net.minecraft.server.level.ServerPlayer trackedEntity) {
                network.sendToAllTracking(new MessageSyncFlight(source, flight), trackedEntity);
            }
        });
    }

    public void invalidateFlightView(Player player) {
    }

    public void sendToServer(Message message) {
        throw new UnsupportedOperationException("sendToServer is client-only");
    }
}
