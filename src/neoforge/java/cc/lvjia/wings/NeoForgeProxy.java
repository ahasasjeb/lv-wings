package cc.lvjia.wings;

import cc.lvjia.wings.server.FlightListenerSupport;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.net.Message;
import cc.lvjia.wings.server.net.Network;
import cc.lvjia.wings.server.net.clientbound.MessageSyncFlight;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;

// NeoForge 平台代理：网络注册和飞行监听器绑定；酿造配方由共享数据包加载。
public class NeoForgeProxy {
    protected final Network network = new Network();

    public void init(IEventBus modBus) {
        this.network.register(modBus);
    }

    // 为玩家 Flight 注册同步监听器，使用 NeoForge 网络 API 发送包
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
