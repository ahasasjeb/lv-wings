package cc.lvjia.wings.server.net.serverbound;

import cc.lvjia.wings.WingsAttachments;
import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.net.Message;
import cc.lvjia.wings.server.net.clientbound.MessageSyncFlight;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 客户端 -> 服务端：请求切换飞行状态。
 * <p>
 * 服务端会再次校验玩家是否允许飞行（例如是否拥有翅膀/是否满足条件）。
 */
public record MessageControlFlying(boolean isFlying) implements Message {
    private static final Logger LOGGER = LogManager.getLogger("WingsNetwork");

    public static final CustomPacketPayload.Type<MessageControlFlying> TYPE = new CustomPacketPayload.Type<>(WingsMod.locate("control_flying"));
    public static final StreamCodec<FriendlyByteBuf, MessageControlFlying> STREAM_CODEC =
            StreamCodec.of((buf, message) -> buf.writeBoolean(message.isFlying()),
                    buf -> new MessageControlFlying(buf.readBoolean()));

    public static void handle(MessageControlFlying message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) {
                LOGGER.warn("Received control_flying from null player");
                return;
            }
            Flight flight = player.getData(WingsAttachments.FLIGHT.get());
            if (!flight.canFly(player)) {
                LOGGER.debug("Player {} failed canFly check, ignoring control_flying", player.getName().getString());
                context.reply(new MessageSyncFlight(player, flight));
                return;
            }
            LOGGER.debug("Player {} {} flying", player.getName().getString(), message.isFlying() ? "started" : "stopped");
            flight.setIsFlying(message.isFlying(), Flight.PlayerSet.ofOthers());
            // Always send the authoritative state back to the initiator to resolve client prediction drift.
            context.reply(new MessageSyncFlight(player, flight));
        });
    }

    @Override
    public CustomPacketPayload.Type<MessageControlFlying> type() {
        return TYPE;
    }
}
