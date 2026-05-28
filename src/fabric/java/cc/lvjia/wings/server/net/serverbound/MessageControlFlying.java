package cc.lvjia.wings.server.net.serverbound;

import cc.lvjia.wings.WingsAttachments;
import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.server.net.Message;
import cc.lvjia.wings.server.net.clientbound.MessageSyncFlight;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * 客户端 -> 服务端：请求切换飞行状态。
 * <p>
 * 服务端会再次校验玩家是否允许飞行（例如是否拥有翅膀/是否满足条件）。
 */
@SuppressWarnings("null")
public record MessageControlFlying(boolean isFlying) implements Message {
    public static final CustomPacketPayload.Type<MessageControlFlying> TYPE = new CustomPacketPayload.Type<>(
            WingsMod.locate("control_flying"));
    public static final StreamCodec<FriendlyByteBuf, MessageControlFlying> STREAM_CODEC = StreamCodec.of(
            (buf, message) -> buf.writeBoolean(message.isFlying()),
            buf -> new MessageControlFlying(buf.readBoolean()));

    public static void handle(MessageControlFlying message, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ServerPlayer player = context.player();
            ControlFlyingMessageHandler.handle(player, message.isFlying(), WingsAttachments::getFlight,
                    (syncPlayer, flight) -> ServerPlayNetworking.send(player, new MessageSyncFlight(syncPlayer, flight)));
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
