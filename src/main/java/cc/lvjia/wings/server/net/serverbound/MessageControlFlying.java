package cc.lvjia.wings.server.net.serverbound;

import cc.lvjia.wings.WingsAttachments;
import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.FlightAnimationState;
import cc.lvjia.wings.server.flight.FlightSpeedAntiCheat;
import cc.lvjia.wings.server.net.Message;
import cc.lvjia.wings.server.net.clientbound.MessageSyncFlight;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
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
public record MessageControlFlying(boolean isFlying) implements Message {
    private static final Logger LOGGER = LogManager.getLogger("WingsNetwork");
    private static final int MIN_CONTROL_INTERVAL_TICKS = 2;
    private static final Map<UUID, Integer> LAST_CONTROL_TICKS = new ConcurrentHashMap<>();

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
            Integer lastControlTick = LAST_CONTROL_TICKS.get(player.getUUID());
            if (lastControlTick != null && player.tickCount - lastControlTick < MIN_CONTROL_INTERVAL_TICKS) {
                return;
            }
            LAST_CONTROL_TICKS.put(player.getUUID(), player.tickCount);
            Flight flight = player.getData(WingsAttachments.FLIGHT.get());
            if (player.isSpectator()) {
                boolean wasFlying = flight.isFlying();
                boolean changed = false;
                if (wasFlying) {
                    LOGGER.debug("Player {} is spectator, forcing wings flight off", player.getName().getString());
                    flight.setIsFlying(false, Flight.PlayerSet.ofAll());
                    changed = true;
                }
                if (flight.getTimeFlying() != 0) {
                    flight.setTimeFlying(0);
                    changed = true;
                }
                if (flight.getAnimationState() != FlightAnimationState.IDLE) {
                    flight.setAnimationState(FlightAnimationState.IDLE);
                    changed = true;
                }
                if (changed && !wasFlying) {
                    flight.sync(Flight.PlayerSet.ofAll());
                }
                FlightSpeedAntiCheat.clear(player);
                context.reply(new MessageSyncFlight(player, flight));
                return;
            }
            if (!flight.canFly(player)) {
                LOGGER.debug("Player {} failed canFly check, ignoring control_flying", player.getName().getString());
                context.reply(new MessageSyncFlight(player, flight));
                return;
            }
            LOGGER.debug("Player {} {} flying", player.getName().getString(), message.isFlying() ? "started" : "stopped");
            // 服务端先写入权威状态，再把同一份快照回发给操作者，修正客户端预测偏差。
            flight.setIsFlying(message.isFlying(), Flight.PlayerSet.ofOthers());
            context.reply(new MessageSyncFlight(player, flight));
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
