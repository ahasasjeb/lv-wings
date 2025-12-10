package cc.lvjia.wings.server.net;

import net.neoforged.fml.LogicalSide;

/**
 * @deprecated Use {@code IPayloadContext} directly.
 */
@Deprecated
public final class ServerMessageContext extends MessageContext {
    @Deprecated
    public ServerMessageContext() {
        throw new UnsupportedOperationException("ServerMessageContext has been removed; use IPayloadContext instead.");
    }

    @Override
    @Deprecated
    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }
}
