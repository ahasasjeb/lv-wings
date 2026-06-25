package cc.lvjia.wings.server.net;

import cc.lvjia.wings.server.net.clientbound.MessageSyncFlight;
import cc.lvjia.wings.server.net.serverbound.MessageControlFlying;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// NeoForge 网络通道：注册 payload + 发送工具
public final class Network {
    private static final Logger LOGGER = LogManager.getLogger("WingsNetwork");
    // payload 注册版本号，协议变更时需更新
    private static final String VERSION = "1";

    public void register(IEventBus modBus) {
        modBus.addListener(this::onRegisterPayloadHandlers);
    }

    // 在 RegisterPayloadHandlersEvent 中注册 payload 编解码和处理器
    private void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);

        // 控制指令走客户端→服务端（optional 表示非必须）
        registrar.optional()
                .playToServer(MessageControlFlying.TYPE, MessageControlFlying.STREAM_CODEC, MessageControlFlying::handle);

        // 飞行快照走服务端→客户端
        registrar.playToClient(MessageSyncFlight.TYPE, MessageSyncFlight.STREAM_CODEC, MessageSyncFlight::handle);

        LOGGER.info("Network payloads registered (version={})", VERSION);
    }

    // 向指定玩家发送消息
    public void sendToPlayer(Message message, ServerPlayer player) {
        LOGGER.debug("Sending {} to player {}", message.type().id(), player.getName().getString());
        PacketDistributor.sendToPlayer(player, message);
    }

    // 向追踪实体的所有玩家广播消息
    public void sendToAllTracking(Message message, Entity entity) {
        LOGGER.debug("Sending {} tracking entity={}", message.type().id(), entity.getName().getString());
        PacketDistributor.sendToPlayersTrackingEntity(entity, message);
    }
}
