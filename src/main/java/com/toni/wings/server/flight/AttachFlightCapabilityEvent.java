package com.toni.wings.server.flight;

/**
 * Legacy Forge event shim retained for binary compatibility. Capability attachment is now handled via
 * NeoForge entity attachments, so this type is no longer instantiated.
 */
@Deprecated(forRemoval = true)
public final class AttachFlightCapabilityEvent {
    private AttachFlightCapabilityEvent() {
        throw new UnsupportedOperationException("Legacy capability attachment event has been removed.");
    }
}
