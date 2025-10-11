package cc.lvjia.wings.server.net;

/**
 * Legacy networking helper retained as a stub for binary compatibility. All mod networking is now handled via
 * {@link Network} and NeoForge payload registration.
 */
@Deprecated
public final class NetBuilder {
    private NetBuilder() {
        throw new UnsupportedOperationException("NetBuilder has been removed; use Network.register instead.");
    }
}
