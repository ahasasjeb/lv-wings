package cc.lvjia.wings.server.net;

import cc.lvjia.wings.server.net.clientbound.MessageSyncFlight;
import cc.lvjia.wings.server.net.serverbound.MessageControlFlying;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 模组网络通道注册与发送工具。
 * <p>
 * 负责在 {@link RegisterPayloadHandlersEvent} 时注册 payload 处理器，并提供常用的发送方法。
 */
public final class Network {
    private static final Logger LOGGER = LogManager.getLogger("WingsNetwork");

    public void register() {
        PayloadTypeRegistry.serverboundPlay().register(MessageControlFlying.TYPE, MessageControlFlying.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(MessageSyncFlight.TYPE, MessageSyncFlight.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(MessageControlFlying.TYPE, MessageControlFlying::handle);
        LOGGER.info("Network payloads registered");
    }

    public void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(MessageSyncFlight.TYPE, MessageSyncFlight::handle);
    }

    public void sendToPlayer(Message message, ServerPlayer player) {
        LOGGER.debug("Sending {} to player {}", message.type().id(), player.getName().getString());
        ServerPlayNetworking.send(player, message);
    }

    public void sendToAllTracking(Message message, Entity entity) {
        LOGGER.debug("Sending {} tracking entity={}", message.type().id(), entity.getName().getString());
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(player, message);
        }
    }
}
