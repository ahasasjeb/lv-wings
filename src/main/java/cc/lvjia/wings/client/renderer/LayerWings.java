package cc.lvjia.wings.client.renderer;

import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.client.ClientProxy;
import cc.lvjia.wings.client.flight.FlightViews;
import cc.lvjia.wings.client.model.ModelWingsAvian;
import cc.lvjia.wings.client.model.ModelWingsInsectoid;
import cc.lvjia.wings.mixin.client.LivingEntityRendererAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Objects;

public final class LayerWings extends RenderLayer<AvatarRenderState, PlayerModel> {

    public static final ModelLayerLocation INSECTOID_WINGS = layer("insectoid_wings");
    public static final ModelLayerLocation AVIAN_WINGS = layer("avian_wings");

    public LayerWings(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    public static void init() {
        ModelLayerRegistry.registerModelLayer(INSECTOID_WINGS, ModelWingsInsectoid::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(AVIAN_WINGS, ModelWingsAvian::createBodyLayer);
        LivingEntityRenderLayerRegistrationCallback.EVENT
                .register((entityType, entityRenderer, registrationHelper, context) -> {
                    if (entityRenderer instanceof AvatarRenderer<?> renderer) {
                        EntityModelSet modelSet = context.getModelSet();
                        ClientProxy.registerWingForms(modelSet);
                        registerPlayerLayers(renderer, modelSet, registrationHelper);
                    }
                });
    }

    private static void registerPlayerLayers(AvatarRenderer<?> renderer, EntityModelSet modelSet,
            LivingEntityRenderLayerRegistrationCallback.RegistrationHelper registrationHelper) {
        List<?> layers = ((LivingEntityRendererAccessor<?, ?, ?>) renderer).wings$getLayers();
        layers.removeIf(layer -> layer instanceof LayerCapeWings || layer instanceof CapeLayer);
        registrationHelper.register(new LayerCapeWings(renderer, modelSet));
        if (layers.stream().noneMatch(LayerWings.class::isInstance)) {
            registrationHelper.register(new LayerWings(renderer));
        }
    }

    private static ModelLayerLocation layer(String name) {
        return layer(name, "main");
    }

    private static ModelLayerLocation layer(String name, String layer) {
        return new ModelLayerLocation(WingsMod.locate(name), layer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight,
            AvatarRenderState state, float limbSwing, float limbSwingAmount) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        if (!(level.getEntity(state.id) instanceof AbstractClientPlayer player)) {
            return;
        }

        if (player.isInvisible()) {
            return;
        }

        FlightViews.get(player).ifPresent(flight -> {
            flight.tick();
            flight.ifFormPresent(form -> {
                float delta = Mth.clamp(state.ageInTicks - player.tickCount, 0.0F, 1.0F);
                poseStack.pushPose();
                if (state.isCrouching) {
                    poseStack.translate(0.0D, 0.2D, 0.0D);
                }
                ModelPart body = this.getParentModel().body;
                body.translateAndRotate(poseStack);
                submitNodeCollector.submitCustomGeometry(poseStack, form.getRenderType(), (pose, buffer) -> {
                    VertexConsumer safeBuffer = Objects.requireNonNull(buffer, "vertex consumer");
                    PoseStack renderStack = new PoseStack();
                    PoseStack.Pose renderPose = renderStack.last();
                    renderPose.pose().set(pose.pose());
                    renderPose.normal().set(pose.normal());
                    form.render(renderStack, SodiumBypassVertexConsumer.wrap(safeBuffer), packedLight,
                            OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F, delta);
                });
                poseStack.popPose();
            });
        });
    }

}
