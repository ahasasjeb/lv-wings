package cc.lvjia.wings.client.renderer;

import cc.lvjia.wings.client.FabricClientProxy;
import cc.lvjia.wings.client.model.ModelWingsAvian;
import cc.lvjia.wings.client.model.ModelWingsInsectoid;
import cc.lvjia.wings.mixin.client.LivingEntityRendererAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("null")
public final class FabricLayerWings extends RenderLayer<AvatarRenderState, PlayerModel> {

    public static final @NonNull ModelLayerLocation INSECTOID_WINGS = WingsLayerRenderer.layer("insectoid_wings");
    public static final @NonNull ModelLayerLocation AVIAN_WINGS = WingsLayerRenderer.layer("avian_wings");

    public FabricLayerWings(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(Objects.requireNonNull(parent, "parent"));
    }

    public static void init() {
        ModelLayerRegistry.registerModelLayer(INSECTOID_WINGS, ModelWingsInsectoid::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(AVIAN_WINGS, ModelWingsAvian::createBodyLayer);
        LivingEntityRenderLayerRegistrationCallback.EVENT
                .register((entityType, entityRenderer, registrationHelper, context) -> {
                    if (entityRenderer instanceof AvatarRenderer<?> renderer) {
                        EntityModelSet modelSet = Objects.requireNonNull(context.getModelSet(), "model set");
                        FabricClientProxy.registerWingForms(modelSet);
                        registerPlayerLayers(renderer, modelSet, registrationHelper);
                    }
                });
    }

    private static void registerPlayerLayers(AvatarRenderer<?> renderer, EntityModelSet modelSet,
                                             LivingEntityRenderLayerRegistrationCallback.RegistrationHelper registrationHelper) {
        Objects.requireNonNull(renderer, "renderer");
        Objects.requireNonNull(modelSet, "model set");
        Objects.requireNonNull(registrationHelper, "registration helper");
        List<?> layers = ((LivingEntityRendererAccessor<?, ?, ?>) renderer).wings$getLayers();
        layers.removeIf(layer -> layer instanceof FabricLayerCapeWings || layer instanceof CapeLayer);
        registrationHelper.register(new FabricLayerCapeWings(renderer, modelSet));
        if (layers.stream().noneMatch(FabricLayerWings.class::isInstance)) {
            registrationHelper.register(new FabricLayerWings(renderer));
        }
    }

    @Override
    public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, int packedLight,
                       @NonNull AvatarRenderState state, float limbSwing, float limbSwingAmount) {
        WingsLayerRenderer.submitWings(poseStack, submitNodeCollector, packedLight, state, this.getParentModel());
    }

}
