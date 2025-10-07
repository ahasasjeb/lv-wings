package com.toni.wings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.toni.wings.WingsMod;
import com.toni.wings.client.flight.FlightViews;
import com.toni.wings.client.model.ModelWingsAvian;
import com.toni.wings.client.model.ModelWingsInsectoid;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import javax.annotation.Nonnull;

public final class LayerWings extends RenderLayer<LivingEntity, HumanoidModel<LivingEntity>> {
    private final TransformFunction transform;

    public static final ModelLayerLocation INSECTOID_WINGS = layer("insectoid_wings");
    public static final ModelLayerLocation AVIAN_WINGS = layer("avian_wings");

    public static void init(IEventBus modBus)
    {
        modBus.addListener(LayerWings::initLayers);
    }

    public LayerWings(LivingEntityRenderer<LivingEntity, HumanoidModel<LivingEntity>> renderer, TransformFunction transform) {
        super(renderer);
        this.transform = transform;
    }

    @Override
    public void render(@Nonnull PoseStack matrixStack, @Nonnull MultiBufferSource buffer, int packedLight, @Nonnull LivingEntity player, float limbSwing, float limbSwingAmount, float delta, float age, float headYaw, float headPitch) {
        if (!player.isInvisible()) {
            FlightViews.get(player).ifPresent(flight -> {
                flight.ifFormPresent(form -> {
                    VertexConsumer builder = buffer.getBuffer(form.getRenderType());
                    matrixStack.pushPose();
                    this.transform.apply(player, matrixStack);
                    form.render(matrixStack, builder, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F, delta);
                    matrixStack.popPose();
                });
            });
        }
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

    @FunctionalInterface
    public interface TransformFunction {
        void apply(LivingEntity player, PoseStack stack);
    }
}
