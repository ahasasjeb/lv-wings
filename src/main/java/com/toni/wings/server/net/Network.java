package com.toni.wings.server.net;

import com.toni.wings.server.net.clientbound.MessageSyncFlight;
import com.toni.wings.server.net.serverbound.MessageControlFlying;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class Network {
    // 飞行快照改用注册表 varint 编码翅膀，协议与旧版不兼容。
    private static final String VERSION = "3";

    public void register(IEventBus modBus) {
        modBus.addListener(this::onRegisterPayloadHandlers);
    }

    private void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);

        registrar.optional()
            .playToServer(MessageControlFlying.TYPE, MessageControlFlying.STREAM_CODEC, MessageControlFlying::handle);

        registrar.playToClient(MessageSyncFlight.TYPE, MessageSyncFlight.STREAM_CODEC, MessageSyncFlight::handle);
    }

    public void sendToServer(Message message) {
        PacketDistributor.sendToServer(message);
    }

    public void sendToPlayer(Message message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public void sendToAllTracking(Message message, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, message);
    }
}
