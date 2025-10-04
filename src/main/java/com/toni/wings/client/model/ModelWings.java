package com.toni.wings.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.toni.wings.client.flight.Animator;
import com.toni.wings.util.MathH;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;


public abstract class ModelWings<A extends Animator> extends Model {
    protected ModelWings(ModelPart root) {
        super(root, RenderType::entityCutout);
    }

    public abstract void render(A animator, float delta, PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha);

    static void setAngles(ModelPart left, ModelPart right, Vec3 angles) {
        right.xRot = (left.xRot = MathH.toRadians((float) angles.x));
        right.yRot = -(left.yRot = MathH.toRadians((float) angles.y));
        right.zRot = -(left.zRot = MathH.toRadians((float) angles.z));
    }
}
