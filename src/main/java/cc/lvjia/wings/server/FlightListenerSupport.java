package cc.lvjia.wings.server;

import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.FlightAbilitySupport;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

// 飞行事件监听器的共享注册逻辑，由 FabricProxy / NeoForgeProxy 调用
public final class FlightListenerSupport {
    private FlightListenerSupport() {
    }

    // 为玩家 Flight 实例注册飞行状态变更和同步监听器
    public static void addFlightListeners(Player player, Flight instance, Sync sync) {
        if (player instanceof ServerPlayer serverPlayer) {
            // 飞行状态变化 → 同步能力（创造飞行/禁止飞行）+ 通知客户端
            instance.registerFlyingListener(isFlying -> {
                FlightAbilitySupport.applyFlyingState(player, isFlying);
                serverPlayer.onUpdateAbilities();
            });
            // 起飞时自动脱离载具
            instance.registerFlyingListener(isFlying -> {
                if (isFlying) {
                    player.removeVehicle();
                }
            });
            // 构建网络发送回调（自己/特定玩家/追踪者）
            Flight.Notifier notifier = Flight.Notifier.of(
                    () -> sync.sendToPlayer(player, instance, serverPlayer),
                    target -> sync.sendToPlayer(player, instance, target),
                    () -> sync.sendToAllTracking(player, instance, serverPlayer)
            );
            instance.registerSyncListener(players -> players.notify(notifier));
            // 新追踪者加入时主动推送当前飞行状态，防止看到过期快照
            instance.sync(Flight.PlayerSet.ofOthers());
        }
    }

    // 网络发送抽象，由平台实现（Fabric / NeoForge 各有一套发送 API）
    public interface Sync {
        void sendToPlayer(Player player, Flight flight, ServerPlayer target);

        void sendToAllTracking(Player player, Flight flight, ServerPlayer trackedEntity);
    }
}
