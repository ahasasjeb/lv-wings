package com.toni.wings.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Wraps a {@link VertexConsumer} without implementing Sodium's VertexBufferWriter interface, forcing Sodium to
 * fall back to vanilla buffering for custom wing quads.
 */
public final class SodiumBypassVertexConsumer implements VertexConsumer {

    private static final ThreadLocal<SodiumBypassVertexConsumer> LOCAL =
        ThreadLocal.withInitial(SodiumBypassVertexConsumer::new);

    public static SodiumBypassVertexConsumer wrap(@Nonnull VertexConsumer delegate) {
        Objects.requireNonNull(delegate, "delegate");
        if (delegate instanceof SodiumBypassVertexConsumer wrapper) {
            return wrapper;
        }
        SodiumBypassVertexConsumer wrapper = LOCAL.get();
        wrapper.delegate = delegate;
        return wrapper;
    }

    private VertexConsumer delegate;

    private SodiumBypassVertexConsumer() {
    }

    public void release() {
        this.delegate = null;
    }

    private VertexConsumer delegate() {
        if (this.delegate == null) {
            throw new IllegalStateException("SodiumBypassVertexConsumer used after release");
        }
        return this.delegate;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        this.delegate().addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        this.delegate().setColor(r, g, b, a);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        this.delegate().setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        this.delegate().setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        this.delegate().setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        this.delegate().setNormal(normalX, normalY, normalZ);
        return this;
    }
}
