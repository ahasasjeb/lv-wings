package cc.lvjia.wings.server.net;

import net.neoforged.fml.LogicalSide;

/**
 * @deprecated Replaced by NeoForge's {@code IPayloadContext}. Retained as a stub for source compatibility.
 */
@Deprecated
public abstract class MessageContext {
    protected MessageContext() {
    }

    public abstract LogicalSide getSide();
}
