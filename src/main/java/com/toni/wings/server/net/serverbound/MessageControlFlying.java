package com.toni.wings.server.net.serverbound;

import com.toni.wings.server.flight.Flight;
import com.toni.wings.server.flight.Flights;
import com.toni.wings.server.net.Message;
import com.toni.wings.server.net.ServerMessageContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public final class MessageControlFlying implements Message {
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
        Flights.get(player).filter(f -> f.canFly(player))
            .ifPresent(flight -> flight.setIsFlying(message.isFlying, Flight.PlayerSet.ofOthers()));
    }
}
