package cc.lvjia.wings.server.net;

import net.neoforged.fml.LogicalSide;

/**
 * @deprecated Use {@code IPayloadContext} directly.
 */
@Deprecated
public final class ClientMessageContext extends MessageContext {
    @Deprecated
    public ClientMessageContext() {
        throw new UnsupportedOperationException("ClientMessageContext has been removed; use IPayloadContext instead.");
    }

    @Override
    @Deprecated
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }
}
