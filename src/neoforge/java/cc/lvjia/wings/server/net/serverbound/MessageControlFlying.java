package cc.lvjia.wings.server.net.serverbound;

import cc.lvjia.wings.WingsAttachments;
import cc.lvjia.wings.WingsMod;
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
    public static final CustomPacketPayload.Type<MessageControlFlying> TYPE = new CustomPacketPayload.Type<>(WingsMod.locate("control_flying"));
    public static final StreamCodec<FriendlyByteBuf, MessageControlFlying> STREAM_CODEC =
            StreamCodec.of((buf, message) -> buf.writeBoolean(message.isFlying()),
                    buf -> new MessageControlFlying(buf.readBoolean()));
    private static final Logger LOGGER = LogManager.getLogger("WingsNetwork");

    public static void handle(MessageControlFlying message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) {
                LOGGER.warn("Received control_flying from null player");
                return;
            }
            ControlFlyingMessageHandler.handle(player, message.isFlying(), p -> p.getData(WingsAttachments.FLIGHT.get()),
                    (syncPlayer, flight) -> context.reply(new MessageSyncFlight(syncPlayer, flight)));
        });
    }

    public static void clearRateLimit(Player player) {
        ControlFlyingMessageHandler.clearRateLimit(player);
    }

    @Override
    public CustomPacketPayload.Type<MessageControlFlying> type() {
        return TYPE;
    }
}
