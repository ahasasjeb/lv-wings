package cc.lvjia.wings.server.net.serverbound;

import cc.lvjia.wings.WingsAttachments;
import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.FlightStateReset;
import cc.lvjia.wings.server.net.Message;
import cc.lvjia.wings.server.net.clientbound.MessageSyncFlight;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
    private static final Logger LOGGER = LogManager.getLogger("WingsNetwork");
    private static final int MIN_CONTROL_INTERVAL_TICKS = 2;
    private static final Map<UUID, Integer> LAST_CONTROL_TICKS = new ConcurrentHashMap<>();

    public static void handle(MessageControlFlying message, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ServerPlayer player = context.player();
            Integer lastControlTick = LAST_CONTROL_TICKS.get(player.getUUID());
            if (lastControlTick != null && player.tickCount - lastControlTick < MIN_CONTROL_INTERVAL_TICKS) {
                return;
            }
            LAST_CONTROL_TICKS.put(player.getUUID(), player.tickCount);
            Flight flight = WingsAttachments.getFlight(player);
            if (FlightStateReset.clearSpectator(player, flight)) {
                LOGGER.debug("Player {} is spectator, forcing wings flight off", player.getName().getString());
                ServerPlayNetworking.send(player, new MessageSyncFlight(player, flight));
                return;
            }
            if (!flight.canFly(player)) {
                LOGGER.debug("Player {} failed canFly check, ignoring control_flying", player.getName().getString());
                ServerPlayNetworking.send(player, new MessageSyncFlight(player, flight));
                return;
            }
            LOGGER.debug("Player {} {} flying", player.getName().getString(),
                    message.isFlying() ? "started" : "stopped");
            // 服务端先写入权威状态，再把同一份快照回发给操作者，修正客户端预测偏差。
            flight.setIsFlying(message.isFlying(), Flight.PlayerSet.ofOthers());
            ServerPlayNetworking.send(player, new MessageSyncFlight(player, flight));
        });
    }

    public static void clearRateLimit(Player player) {
        LAST_CONTROL_TICKS.remove(player.getUUID());
    }

    @Override
    public CustomPacketPayload.Type<MessageControlFlying> type() {
        return TYPE;
    }
}
