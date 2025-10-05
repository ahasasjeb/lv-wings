package com.toni.wings.util;

/**
 * Legacy Forge capability helper placeholder retained to avoid breaking binary compatibility.
 * <p>
 * Capability management is now handled via NeoForge attachments and entity capabilities, so this
 * class is intentionally empty and should not be used.
 */
public final class CapabilityHolder {
    private CapabilityHolder() {
        throw new UnsupportedOperationException("Legacy capability holder has been removed; use attachments instead.");
    }
}
