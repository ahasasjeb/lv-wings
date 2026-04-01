package cc.lvjia.wings.server.net.clientbound;

import cc.lvjia.wings.WingsAttachments;
import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.FlightDefault;
import cc.lvjia.wings.server.flight.Flights;
import cc.lvjia.wings.server.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 服务端 -> 客户端：同步指定玩家的飞行数据。
 * <p>
 * 客户端收到后会把数据写入玩家的 attachment，并刷新相关渲染/视图缓存。
 */
public record MessageSyncFlight(int playerId, Flight flight) implements Message {
    private static final Logger LOGGER = LogManager.getLogger("WingsNetwork");

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

    public static void handle(MessageSyncFlight message, IPayloadContext context) {
        // 网络线程回调：通过 enqueueWork 切到主线程安全更新世界/实体数据。
        context.enqueueWork(() -> {
            var level = context.player().level();
            if (level == null) {
                LOGGER.warn("Received sync_flight but level is null");
                return;
            }
            var entity = level.getEntity(message.playerId());
            if (!(entity instanceof Player player)) {
                LOGGER.warn("Received sync_flight for invalid entity id={}", message.playerId());
                return;
            }
            Flight flight = Flights.get(player).orElse(null);
            if (flight == null) {
                LOGGER.debug("Creating new flight attachment for player {}", player.getName().getString());
                flight = new FlightDefault();
                player.setData(WingsAttachments.FLIGHT.get(), flight);
            }
            flight.clone(message.flight());
            WingsMod.instance().invalidateFlightView(player);
            LOGGER.debug("Synced flight data for player {} (flying={}, wing={})",
                    player.getName().getString(), flight.isFlying(), flight.getWing());
        });
    }

    @Override
    public CustomPacketPayload.Type<MessageSyncFlight> type() {
        return TYPE;
    }
}
