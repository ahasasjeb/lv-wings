package cc.lvjia.wings.client.apparatus;

import cc.lvjia.wings.client.flight.Animator;
import cc.lvjia.wings.client.model.ModelWings;
import cc.lvjia.wings.server.apparatus.FlightApparatus;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public final class WingForm<A extends Animator> {
    private static final Map<FlightApparatus, WingForm<?>> FORMS = new HashMap<>();

    private final Supplier<A> animator;
    private final Identifier texture;
    private final Supplier<RenderType> renderType;
    private ModelWings<A> model;

    private WingForm(Supplier<A> animator, ModelWings<A> model, Identifier texture, Supplier<RenderType> renderType) {
        this.animator = Objects.requireNonNull(animator);

        this.model = Objects.requireNonNull(model);
        this.texture = Objects.requireNonNull(texture);
        this.renderType = Objects.requireNonNull(renderType);
    }

    public static <A extends Animator> WingForm<A> of(Supplier<A> animator, ModelWings<A> model, Identifier texture) {
        return new WingForm<>(animator, model, texture, () -> RenderTypes.entityCutout(texture));
    }

    public static <A extends Animator> WingForm<A> of(Supplier<A> animator, ModelWings<A> model, Identifier texture, Supplier<RenderType> renderType) {
        return new WingForm<>(animator, model, texture, renderType);
    }

    public static Optional<WingForm<?>> get(FlightApparatus wings) {
        return Optional.ofNullable(FORMS.get(wings));
    }

    public static void register(FlightApparatus wings, WingForm<?> form) {
        FORMS.put(wings, form);
    }

    public static Map<FlightApparatus, WingForm<?>> getFormsMap() {
        return FORMS;
    }

    public static boolean isEmpty() {
        return FORMS.isEmpty();
    }

    public A createAnimator() {
        return this.animator.get();
    }

    public ModelWings<A> getModel() {
        return this.model;
    }

    public void setModel(ModelWings<A> model) {
        this.model = model;
    }

    public Identifier getTexture() {
        return this.texture;
    }

    public RenderType getRenderType() {
        return this.renderType.get();
    }


}
