package com.toni.wings.server.net;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.CustomPayloadEvent;
import net.neoforged.fml.LogicalSide;

import java.util.Objects;

public class ServerMessageContext extends MessageContext {
    public ServerMessageContext(CustomPayloadEvent.Context context) {
        super(context);
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }

    public MinecraftServer getServer() {
        return this.getPlayer().server;
    }

    public ServerLevel getWorld() {
        return this.getPlayer().serverLevel();
    }

    public ServerPlayer getPlayer() {
        return Objects.requireNonNull(this.context.getSender());
    }
}
