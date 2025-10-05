package com.toni.wings.server.net;

import net.neoforged.neoforge.network.event.CustomPayloadEvent;
import net.neoforged.fml.LogicalSide;

public abstract class MessageContext {
    protected final CustomPayloadEvent.Context context;

    public MessageContext(CustomPayloadEvent.Context context) {
        this.context = context;
    }

    public abstract LogicalSide getSide();
}
