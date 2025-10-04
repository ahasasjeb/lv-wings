package com.toni.wings.server.net;

import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.LogicalSide;

public abstract class MessageContext {
    protected final CustomPayloadEvent.Context context;

    public MessageContext(CustomPayloadEvent.Context context) {
        this.context = context;
    }

    public abstract LogicalSide getSide();
}
