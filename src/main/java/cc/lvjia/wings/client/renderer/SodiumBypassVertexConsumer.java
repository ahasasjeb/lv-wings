package cc.lvjia.wings.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Wraps a {@link VertexConsumer} without implementing Sodium's VertexBufferWriter interface, forcing Sodium to
 * fall back to vanilla buffering for custom wing quads.
 */
public final class SodiumBypassVertexConsumer implements VertexConsumer {

    private static final Map<VertexConsumer, SodiumBypassVertexConsumer> CACHE = Collections.synchronizedMap(new IdentityHashMap<>());
    private final VertexConsumer delegate;

    private SodiumBypassVertexConsumer(VertexConsumer delegate) {
        this.delegate = delegate;
    }

    public static VertexConsumer wrap(@Nonnull VertexConsumer delegate) {
        if (delegate instanceof SodiumBypassVertexConsumer) {
            return delegate;
        }
        return CACHE.computeIfAbsent(delegate, SodiumBypassVertexConsumer::new);
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        this.delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        this.delegate.setColor(r, g, b, a);
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
