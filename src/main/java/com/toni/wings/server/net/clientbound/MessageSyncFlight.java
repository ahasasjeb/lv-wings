package com.toni.wings.server.net.clientbound;

import com.toni.wings.WingsMod;
import com.toni.wings.server.flight.Flight;
import com.toni.wings.server.flight.FlightDefault;
import com.toni.wings.server.flight.Flights;
import com.toni.wings.server.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageSyncFlight(int playerId, Flight flight) implements Message {
    public static final CustomPacketPayload.Type<MessageSyncFlight> TYPE = new CustomPacketPayload.Type<>(WingsMod.locate("sync_flight"));
    public static final StreamCodec<FriendlyByteBuf, MessageSyncFlight> STREAM_CODEC =
        StreamCodec.of((buf, message) -> {
            buf.writeVarInt(message.playerId());
            message.flight().serialize(buf);
        }, buf -> {
            int playerId = buf.readVarInt();
            FlightDefault flight = new FlightDefault();
            flight.deserialize(buf);
            return new MessageSyncFlight(playerId, flight);
        });

    public MessageSyncFlight(Player player, Flight flight) {
        this(player.getId(), flight);
    }

    @Override
    public CustomPacketPayload.Type<MessageSyncFlight> type() {
        return TYPE;
    }

    public static void handle(MessageSyncFlight message, IPayloadContext context) {
        var level = context.player().level();
        if (level != null) {
            Flights.ifPlayer(level.getEntity(message.playerId()),
                (player, flight) -> flight.clone(message.flight())
            );
        }
    }
}
