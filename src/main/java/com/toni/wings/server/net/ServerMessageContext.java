package com.toni.wings.server.net;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;

public class ServerMessageContext extends MessageContext {
    public ServerMessageContext(NetworkEvent.Context context) {
        super(context);
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }

    public ServerPlayer getPlayer() {
        return Objects.requireNonNull(this.context.getSender());
    }
}
