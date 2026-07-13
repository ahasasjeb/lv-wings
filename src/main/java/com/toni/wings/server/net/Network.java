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
    // 飞行快照新增动画状态字段，旧版协议不能与当前版混用。
    private static final String VERSION = "2";

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
