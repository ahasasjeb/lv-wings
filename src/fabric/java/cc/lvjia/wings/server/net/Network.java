package cc.lvjia.wings.server.net;

import cc.lvjia.wings.server.net.clientbound.MessageSyncFlight;
import cc.lvjia.wings.server.net.serverbound.MessageControlFlying;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Fabric 网络通道：注册 payload + 发送工具
@SuppressWarnings("null")
public final class Network {
    private static final Logger LOGGER = LogManager.getLogger("WingsNetwork");

    // 注册客户端↔服务端的 payload 类型及处理器
    public void register() {
        PayloadTypeRegistry.serverboundPlay().register(MessageControlFlying.TYPE, MessageControlFlying.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(MessageSyncFlight.TYPE, MessageSyncFlight.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(MessageControlFlying.TYPE, MessageControlFlying::handle);
        LOGGER.info("Network payloads registered");
    }

    // 向指定玩家发送消息
    public void sendToPlayer(Message message, ServerPlayer player) {
        LOGGER.debug("Sending {} to player {}", message.type().id(), player.getName().getString());
        ServerPlayNetworking.send(player, message);
    }

    // 向追踪实体的所有玩家广播消息
    public void sendToAllTracking(Message message, Entity entity) {
        LOGGER.debug("Sending {} tracking entity={}", message.type().id(), entity.getName().getString());
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(player, message);
        }
    }
}
