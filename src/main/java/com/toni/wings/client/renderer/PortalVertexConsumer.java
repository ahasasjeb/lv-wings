package com.toni.wings.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * A vertex consumer wrapper that forwards only positional information to the delegate.
 * <p>
 * Sodium expects {@link net.minecraft.client.renderer.RenderType#endPortal()} geometry to supply
 * position-only vertices. The vanilla renderer tolerates the additional attributes that our wing models emit,
 * but Sodium's optimized upload path misinterprets the extra data which collapses the portal wings.
 * Wrapping the delegate ensures we always emit bare positions for portal layers while preserving the vanilla
 * behaviour for every other render type.
 * <p>
 * This class implements Sodium's VertexBufferWriter interface via duck typing to avoid compilation dependencies.
 * When Sodium is present, it will detect these methods and avoid warning about unsupported vertex consumers.
 */
public final class PortalVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;

    public PortalVertexConsumer(VertexConsumer delegate) {
        this.delegate = delegate;
    }

    /**
     * Sodium VertexBufferWriter compatibility method.
     * Returns false to indicate we don't support intrinsics, which tells Sodium to use the
     * normal vertex-by-vertex rendering path instead of trying to use fast bulk uploads.
     * This is necessary because we're filtering vertex attributes.
     */
    public boolean canUseIntrinsics() {
        return false;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        this.delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public void addVertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
        // Only forward position data for endPortal rendering
        this.delegate.addVertex(x, y, z);
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        // Discard color data for endPortal
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        // Discard UV data for endPortal
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        // Discard lightmap data for endPortal
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        // Discard overlay data for endPortal
        return this;
    }

    @Override
    public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        // Discard normal data for endPortal
        return this;
    }
}
