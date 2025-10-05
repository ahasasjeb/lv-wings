package com.toni.wings.server.net.serverbound;

import com.toni.wings.WingsMod;
import com.toni.wings.server.flight.Flight;
import com.toni.wings.server.flight.Flights;
import com.toni.wings.server.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageControlFlying(boolean isFlying) implements Message {
    public static final CustomPacketPayload.Type<MessageControlFlying> TYPE = new CustomPacketPayload.Type<>(WingsMod.locate("control_flying"));
    public static final StreamCodec<FriendlyByteBuf, MessageControlFlying> STREAM_CODEC =
        StreamCodec.of((buf, message) -> buf.writeBoolean(message.isFlying()),
            buf -> new MessageControlFlying(buf.readBoolean()));

    @Override
    public CustomPacketPayload.Type<MessageControlFlying> type() {
        return TYPE;
    }

    public static void handle(MessageControlFlying message, IPayloadContext context) {
        Player player = context.player();
        Flights.get(player).filter(f -> f.canFly(player))
            .ifPresent(flight -> flight.setIsFlying(message.isFlying(), Flight.PlayerSet.ofOthers()));
    }
}
