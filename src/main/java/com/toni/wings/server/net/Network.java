package com.toni.wings.server.net;

import com.toni.wings.WingsMod;
import com.toni.wings.server.net.clientbound.MessageSyncFlight;
import com.toni.wings.server.net.serverbound.MessageControlFlying;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Network {
    private final SimpleChannel network = new NetBuilder(WingsMod.locate("net"))
        .version(1).optionalServer().requiredClient()
        .serverbound(MessageControlFlying::new).consumer(() -> MessageControlFlying::handle)
        .clientbound(MessageSyncFlight::new).consumer(() -> MessageSyncFlight::handle)
        .build();

    public void sendToServer(Message message) {
        network.sendToServer(message);
    }

    public void sendToPlayer(Message message, ServerPlayer player) {
        network.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public void sendToAllTracking(Message message, Entity entity) {
        network.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }
}
