package cc.lvjia.wings.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Wraps a {@link VertexConsumer} without implementing Sodium's
 * VertexBufferWriter interface, forcing Sodium to
 * fall back to vanilla buffering for custom wing quads.
 */
public final class SodiumBypassVertexConsumer implements VertexConsumer {

    private static final ThreadLocal<SodiumBypassVertexConsumer> LOCAL =
            ThreadLocal.withInitial(SodiumBypassVertexConsumer::new);

    private @Nullable VertexConsumer delegate;

    private SodiumBypassVertexConsumer() {
    }

    public static @NonNull SodiumBypassVertexConsumer wrap(@NonNull VertexConsumer delegate) {
        delegate = Objects.requireNonNull(delegate, "delegate");
        if (delegate instanceof SodiumBypassVertexConsumer wrapper) {
            return wrapper;
        }
        SodiumBypassVertexConsumer wrapper = LOCAL.get();
        wrapper.delegate = delegate;
        return wrapper;
    }

    public void release() {
        this.delegate = null;
    }

    private @NonNull VertexConsumer delegate() {
        VertexConsumer delegate = this.delegate;
        if (delegate == null) {
            throw new IllegalStateException("SodiumBypassVertexConsumer used after release");
        }
        return delegate;
    }

    @Override
    public @NonNull VertexConsumer addVertex(float x, float y, float z) {
        this.delegate().addVertex(x, y, z);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setColor(int r, int g, int b, int a) {
        this.delegate().setColor(r, g, b, a);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setColor(int color) {
        this.delegate().setColor(color);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setUv(float u, float v) {
        this.delegate().setUv(u, v);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setUv1(int u, int v) {
        this.delegate().setUv1(u, v);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setUv2(int u, int v) {
        this.delegate().setUv2(u, v);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        this.delegate().setNormal(normalX, normalY, normalZ);
        return this;
    }

    @Override
    public @NonNull VertexConsumer setLineWidth(float width) {
        this.delegate().setLineWidth(width);
        return this;
    }
}
