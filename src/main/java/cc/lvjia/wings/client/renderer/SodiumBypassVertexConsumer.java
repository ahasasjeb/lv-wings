package cc.lvjia.wings.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * Wraps a {@link VertexConsumer} without implementing Sodium's
 * VertexBufferWriter interface, forcing Sodium to
 * fall back to vanilla buffering for custom wing quads.
 */
public final class SodiumBypassVertexConsumer implements VertexConsumer {

    private static final Map<@NonNull VertexConsumer, @NonNull SodiumBypassVertexConsumer> CACHE = Collections
            .synchronizedMap(new WeakHashMap<>());
    private final @NonNull VertexConsumer delegate;

    private SodiumBypassVertexConsumer(@NonNull VertexConsumer delegate) {
        this.delegate = delegate;
    }

    public static @NonNull VertexConsumer wrap(@NonNull VertexConsumer delegate) {
        delegate = Objects.requireNonNull(delegate, "delegate");
        if (delegate instanceof SodiumBypassVertexConsumer) {
            return delegate;
        }
        return Objects.requireNonNull(CACHE.computeIfAbsent(delegate,
                key -> new SodiumBypassVertexConsumer(Objects.requireNonNull(key, "delegate"))),
                "vertex consumer wrapper");
    }

    @Override
    public @NonNull VertexConsumer addVertex(float x, float y, float z) {
        this.delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setColor(int r, int g, int b, int a) {
        this.delegate.setColor(r, g, b, a);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setColor(int color) {
        this.delegate.setColor(color);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setUv(float u, float v) {
        this.delegate.setUv(u, v);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setUv1(int u, int v) {
        this.delegate.setUv1(u, v);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setUv2(int u, int v) {
        this.delegate.setUv2(u, v);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        this.delegate.setNormal(normalX, normalY, normalZ);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setLineWidth(float width) {
        this.delegate.setLineWidth(width);
        return this;
    }
}
