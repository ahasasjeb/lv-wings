package cc.lvjia.wings.client.apparatus;

import cc.lvjia.wings.client.flight.Animator;
import cc.lvjia.wings.client.model.ModelWings;
import cc.lvjia.wings.server.apparatus.FlightApparatus;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@SuppressWarnings("null")
public final class WingForm<A extends @NonNull Animator> {
    private static final Map<@NonNull FlightApparatus, @NonNull WingForm<? extends @NonNull Animator>> FORMS = new HashMap<>();

    private final @NonNull Supplier<@NonNull A> animator;
    private final @NonNull Supplier<@NonNull RenderType> renderType;
    private final @NonNull ModelWings<A> model;

    private WingForm(@NonNull Supplier<@NonNull A> animator, @NonNull ModelWings<A> model,
            @NonNull Identifier texture, @NonNull Supplier<@NonNull RenderType> renderType) {
        this.animator = Objects.requireNonNull(animator);

        this.model = Objects.requireNonNull(model);
        Objects.requireNonNull(texture);
        this.renderType = Objects.requireNonNull(renderType);
    }

    public static <A extends @NonNull Animator> @NonNull WingForm<A> of(@NonNull Supplier<@NonNull A> animator,
            @NonNull ModelWings<A> model, @NonNull Identifier texture) {
        return new WingForm<>(animator, model, texture, () -> RenderTypes.entityCutout(texture));
    }

    public static <A extends @NonNull Animator> @NonNull WingForm<A> of(@NonNull Supplier<@NonNull A> animator,
            @NonNull ModelWings<A> model, @NonNull Identifier texture,
            @NonNull Supplier<@NonNull RenderType> renderType) {
        return new WingForm<>(animator, model, texture, renderType);
    }

    public static @NonNull Optional<@NonNull WingForm<? extends @NonNull Animator>> get(@NonNull FlightApparatus wings) {
        return Optional.ofNullable(FORMS.get(wings));
    }

    public static void register(@NonNull FlightApparatus wings, @NonNull WingForm<? extends @NonNull Animator> form) {
        FORMS.put(wings, form);
    }

    public static void clear() {
        FORMS.clear();
    }

    public @NonNull A createAnimator() {
        return Objects.requireNonNull(this.animator.get(), "animator");
    }

    public @NonNull ModelWings<A> getModel() {
        return this.model;
    }

    public @NonNull RenderType getRenderType() {
        return Objects.requireNonNull(this.renderType.get(), "render type");
    }


}
