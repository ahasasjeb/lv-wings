package com.toni.wings.server.net;

import net.minecraft.network.FriendlyByteBuf;

public interface Message {
    void encode(FriendlyByteBuf buf);

    void decode(FriendlyByteBuf buf);
}
