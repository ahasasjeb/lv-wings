package com.toni.wings.client.flight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.toni.wings.util.function.FloatConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface FlightView {
    void ifFormPresent(Consumer<FormRenderer> consumer);

    void tick();

    void tickEyeHeight(float value, FloatConsumer valueOut);

    interface FormRenderer {
        ResourceLocation getTexture();

        RenderType getRenderType();

        void render(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha, float delta);
    }
}
