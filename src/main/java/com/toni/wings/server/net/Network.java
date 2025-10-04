package com.toni.wings.server.net;

import com.toni.wings.WingsMod;
import com.toni.wings.server.net.clientbound.MessageSyncFlight;
import com.toni.wings.server.net.serverbound.MessageControlFlying;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public final class Network {
    private final SimpleChannel network = new NetBuilder(WingsMod.locate("net"))
        .version(1).optionalServer().requiredClient()
        .serverbound(MessageControlFlying::new).consumer(() -> MessageControlFlying::handle)
        .clientbound(MessageSyncFlight::new).consumer(() -> MessageSyncFlight::handle)
        .build();

    public void sendToServer(Message message) {
        network.send(message, PacketDistributor.SERVER.noArg());
    }

    public void sendToPlayer(Message message, ServerPlayer player) {
        network.send(message, PacketDistributor.PLAYER.with(player));
    }

    public void sendToAllTracking(Message message, Entity entity) {
        network.send(message, PacketDistributor.TRACKING_ENTITY.with(entity));
    }
}
