package com.toni.wings.server.net.serverbound;

import com.toni.wings.server.flight.Flight;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端飞行控制入口：限制控制包频率，并始终以服务端状态回应客户端预测。
 */
public final class ControlFlyingMessageHandler {
    private static final int MIN_CONTROL_INTERVAL_TICKS = 2;
    private static final Map<UUID, Integer> LAST_CONTROL_TICKS = new ConcurrentHashMap<>();

    private ControlFlyingMessageHandler() {
    }

    public static void handle(Player player, boolean requestedFlying, Flight flight, FlightSync reply) {
        UUID playerId = player.getUUID();
        Integer lastControlTick = LAST_CONTROL_TICKS.get(playerId);
        if (lastControlTick != null) {
            int elapsedTicks = player.tickCount - lastControlTick;
            if (elapsedTicks >= 0 && elapsedTicks < MIN_CONTROL_INTERVAL_TICKS) {
                reply.send(player, flight);
                return;
            }
        }
        LAST_CONTROL_TICKS.put(playerId, player.tickCount);

        if (player.isSpectator() || !flight.canFly(player)) {
            flight.setIsFlying(false, Flight.PlayerSet.ofOthers());
            reply.send(player, flight);
            return;
        }

        flight.setIsFlying(requestedFlying, Flight.PlayerSet.ofOthers());
        reply.send(player, flight);
    }

    public static void clearRateLimit(Player player) {
        LAST_CONTROL_TICKS.remove(player.getUUID());
    }

    @FunctionalInterface
    public interface FlightSync {
        void send(Player player, Flight flight);
    }
}
