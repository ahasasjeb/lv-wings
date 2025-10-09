package com.toni.wings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.toni.wings.WingsMod;
import com.toni.wings.client.flight.FlightViews;
import com.toni.wings.client.model.ModelWingsAvian;
import com.toni.wings.client.model.ModelWingsInsectoid;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.IEventBus;

import javax.annotation.Nonnull;

public final class LayerWings extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public static final ModelLayerLocation INSECTOID_WINGS = layer("insectoid_wings");
    public static final ModelLayerLocation AVIAN_WINGS = layer("avian_wings");

    public static void init(IEventBus modBus)
    {
        modBus.addListener(LayerWings::initLayers);
    }

    public LayerWings(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int packedLight, @Nonnull AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (player.isInvisible()) {
            return;
        }

        FlightViews.get(player).ifPresent(flight -> flight.ifFormPresent(form -> {
            float delta = Mth.clamp(ageInTicks - player.tickCount, 0.0F, 1.0F);
            VertexConsumer builder = buffer.getBuffer(form.getRenderType());
            poseStack.pushPose();
            if (player.isCrouching()) {
                poseStack.translate(0.0D, 0.2D, 0.0D);
            }
            PlayerModel<AbstractClientPlayer> parentModel = this.getParentModel();
            ModelPart body = parentModel.body;
            body.translateAndRotate(poseStack);
            form.render(poseStack, builder, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F, delta);
            poseStack.popPose();
        }));
    }

    public static void initLayers(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(INSECTOID_WINGS, ModelWingsInsectoid::createBodyLayer);
        event.registerLayerDefinition(AVIAN_WINGS, ModelWingsAvian::createBodyLayer);
    }

    private static ModelLayerLocation layer(String name)
    {
        return layer(name, "main");
    }

    private static ModelLayerLocation layer(String name, String layer)
    {
        return new ModelLayerLocation(WingsMod.locate(name), layer);
    }

}
