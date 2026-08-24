package com.toni.wings.server.net.serverbound;

import com.toni.wings.server.flight.Flight;
import com.toni.wings.server.flight.Flights;
import com.toni.wings.server.net.Message;
import com.toni.wings.server.net.ServerMessageContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MessageControlFlying implements Message {
    private static final long MIN_CONTROL_INTERVAL_TICKS = 2L;

    private static final Map<UUID, Long> LAST_CONTROL_TICKS = new ConcurrentHashMap<>();

    private boolean isFlying;

    public MessageControlFlying() {
    }

    public MessageControlFlying(boolean isFlying) {
        this.isFlying = isFlying;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.isFlying);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        this.isFlying = buf.readBoolean();
    }

    public static void handle(MessageControlFlying message, ServerMessageContext context) {
        Player player = context.getPlayer();
        Flights.get(player).ifPresent(flight -> {
            long currentTick = context.getServer().getTickCount();
            Long lastControlTick = LAST_CONTROL_TICKS.get(player.getUUID());
            if (lastControlTick != null && currentTick - lastControlTick < MIN_CONTROL_INTERVAL_TICKS) {
                flight.sync(Flight.PlayerSet.ofSelf());
                return;
            }
            LAST_CONTROL_TICKS.put(player.getUUID(), currentTick);

            if (player.isSpectator()) {
                flight.setIsFlying(false, Flight.PlayerSet.ofOthers());
                flight.sync(Flight.PlayerSet.ofSelf());
                return;
            }
            if (message.isFlying && !flight.canFly(player)) {
                flight.setIsFlying(false, Flight.PlayerSet.ofOthers());
                flight.sync(Flight.PlayerSet.ofSelf());
                return;
            }

            flight.setIsFlying(message.isFlying, Flight.PlayerSet.ofOthers());
            flight.sync(Flight.PlayerSet.ofSelf());
        });
    }

    public static void clearRateLimit(Player player) {
        LAST_CONTROL_TICKS.remove(player.getUUID());
    }
}
