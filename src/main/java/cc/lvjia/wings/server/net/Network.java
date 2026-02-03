package cc.lvjia.wings.server.net;

import cc.lvjia.wings.server.net.clientbound.MessageSyncFlight;
import cc.lvjia.wings.server.net.serverbound.MessageControlFlying;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 模组网络通道注册与发送工具。
 * <p>
 * 负责在 {@link RegisterPayloadHandlersEvent} 时注册 payload 处理器，并提供常用的发送方法。
 */
public final class Network {
    /**
     * payload 注册版本号：用于网络协议变更时进行兼容控制。
     */
    private static final String VERSION = "1";

    public void register(IEventBus modBus) {
        modBus.addListener(this::onRegisterPayloadHandlers);
    }

    private void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);

        registrar.optional()
                .playToServer(MessageControlFlying.TYPE, MessageControlFlying.STREAM_CODEC, MessageControlFlying::handle);

        registrar.playToClient(MessageSyncFlight.TYPE, MessageSyncFlight.STREAM_CODEC, MessageSyncFlight::handle);
    }

    public void sendToPlayer(Message message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public void sendToAllTracking(Message message, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, message);
    }
}
