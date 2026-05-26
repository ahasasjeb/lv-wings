package cc.lvjia.wings.client;

import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.client.apparatus.WingForm;
import cc.lvjia.wings.client.flight.Animator;
import cc.lvjia.wings.client.flight.AnimatorAvian;
import cc.lvjia.wings.client.flight.AnimatorInsectoid;
import cc.lvjia.wings.client.model.ModelWings;
import cc.lvjia.wings.client.model.ModelWingsAvian;
import cc.lvjia.wings.client.model.ModelWingsInsectoid;
import cc.lvjia.wings.server.apparatus.FlightApparatus;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public final class WingFormRegistrar {
    private WingFormRegistrar() {
    }

    public static void register(@NonNull EntityModelSet modelSet, @NonNull ModelLayerLocation avianWings,
            @NonNull ModelLayerLocation insectoidWings) {
        WingForm.clear();
        WingForm.register(WingsMod.ANGEL_WINGS, createAvianWings(modelSet, avianWings, wingKey(WingsMod.ANGEL_WINGS)));
        WingForm.register(WingsMod.PARROT_WINGS, createAvianWings(modelSet, avianWings, wingKey(WingsMod.PARROT_WINGS)));
        WingForm.register(WingsMod.BAT_WINGS, createAvianWings(modelSet, avianWings, wingKey(WingsMod.BAT_WINGS)));
        WingForm.register(WingsMod.BLUE_BUTTERFLY_WINGS,
                createInsectoidWings(modelSet, insectoidWings, wingKey(WingsMod.BLUE_BUTTERFLY_WINGS)));
        WingForm.register(WingsMod.DRAGON_WINGS, createAvianWings(modelSet, avianWings, wingKey(WingsMod.DRAGON_WINGS)));
        WingForm.register(WingsMod.EVIL_WINGS, createAvianWings(modelSet, avianWings, wingKey(WingsMod.EVIL_WINGS)));
        WingForm.register(WingsMod.FAIRY_WINGS,
                createInsectoidWings(modelSet, insectoidWings, wingKey(WingsMod.FAIRY_WINGS)));
        WingForm.register(WingsMod.FIRE_WINGS, createAvianWings(modelSet, avianWings, wingKey(WingsMod.FIRE_WINGS)));
        WingForm.register(WingsMod.MONARCH_BUTTERFLY_WINGS,
                createInsectoidWings(modelSet, insectoidWings, wingKey(WingsMod.MONARCH_BUTTERFLY_WINGS)));
        WingForm.register(WingsMod.SLIME_WINGS,
                createInsectoidWings(modelSet, insectoidWings, wingKey(WingsMod.SLIME_WINGS)));
        WingForm.register(WingsMod.LVJIA_SUPER_WINGS,
                createEndPortalWings(modelSet, avianWings, wingKey(WingsMod.LVJIA_SUPER_WINGS)));
    }

    private static @NonNull WingForm<@NonNull AnimatorAvian> createAvianWings(@NonNull EntityModelSet modelSet,
            @NonNull ModelLayerLocation layer, @NonNull Identifier name) {
        return createWings(name, AnimatorAvian::new, new ModelWingsAvian(modelSet.bakeLayer(layer)));
    }

    private static @NonNull WingForm<@NonNull AnimatorAvian> createEndPortalWings(@NonNull EntityModelSet modelSet,
            @NonNull ModelLayerLocation layer, @NonNull Identifier name) {
        return createWings(name, AnimatorAvian::new, new ModelWingsAvian(modelSet.bakeLayer(layer)),
                RenderTypes::endPortal);
    }

    private static @NonNull WingForm<@NonNull AnimatorInsectoid> createInsectoidWings(
            @NonNull EntityModelSet modelSet, @NonNull ModelLayerLocation layer, @NonNull Identifier name) {
        return createWings(name, AnimatorInsectoid::new, new ModelWingsInsectoid(modelSet.bakeLayer(layer)));
    }

    private static <A extends @NonNull Animator> @NonNull WingForm<A> createWings(@NonNull Identifier name,
            @NonNull Supplier<@NonNull A> animator, @NonNull ModelWings<A> model) {
        return createWings(name, animator, model, null);
    }

    private static <A extends @NonNull Animator> @NonNull WingForm<A> createWings(@NonNull Identifier name,
            @NonNull Supplier<@NonNull A> animator, @NonNull ModelWings<A> model,
            @Nullable Supplier<@NonNull RenderType> renderType) {
        String texturePath = "textures/entity/" + name.getPath() + ".png";
        Identifier texture = Objects.requireNonNull(
                Identifier.tryBuild(Objects.requireNonNull(name.getNamespace(), "namespace"), texturePath),
                "Invalid texture path: " + texturePath);
        Supplier<RenderType> actualRenderType = renderType != null ? renderType : () -> RenderTypes.entityCutout(texture);
        return WingForm.of(animator, model, texture, actualRenderType);
    }

    private static @NonNull Identifier wingKey(FlightApparatus wing) {
        return Objects.requireNonNull(WingsMod.WINGS.getKey(wing), "Wing is not registered: " + wing);
    }
}
