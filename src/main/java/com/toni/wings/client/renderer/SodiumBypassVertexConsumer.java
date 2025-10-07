package com.toni.wings.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.Objects;

/**
 * Delegating {@link VertexConsumer} wrapper that intentionally omits Sodium's {@code VertexBufferWriter}
 * interface so that callers fall back to the vanilla vertex emission path. This preserves the custom
 * polygon data used by the wing models while keeping Sodium optional.
 */
public final class SodiumBypassVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;

    private SodiumBypassVertexConsumer(VertexConsumer delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    public static VertexConsumer wrap(VertexConsumer delegate) {
        if (delegate instanceof SodiumBypassVertexConsumer bypass) {
            return bypass;
        }
        return new SodiumBypassVertexConsumer(delegate);
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        this.delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        this.delegate.setColor(red, green, blue, alpha);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        this.delegate.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        this.delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        this.delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        this.delegate.setNormal(normalX, normalY, normalZ);
        return this;
    }
}
