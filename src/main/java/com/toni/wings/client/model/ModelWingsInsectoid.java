package com.toni.wings.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.toni.wings.client.flight.AnimatorInsectoid;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import javax.annotation.Nonnull;

@SuppressWarnings({"null", "nullness"})
public final class ModelWingsInsectoid extends ModelWings<AnimatorInsectoid> {
    private final ModelPart wingLeft;

    private final ModelPart wingRight;

    public ModelWingsInsectoid(ModelPart root) {
        this.wingLeft = root.getChild("WingLeft");
        this.wingRight = root.getChild("WingRight");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("WingLeft", CubeListBuilder.create().texOffs(0, 0).addBox(0, -8, 0, 19, 24, 0, new CubeDeformation(0.0F)), PartPose.offset(0, 2, 3.5F));

        partdefinition.addOrReplaceChild("WingRight", CubeListBuilder.create().texOffs(0, 24).addBox(-19, -8, 0, 19, 24, 0, new CubeDeformation(0.0F)), PartPose.offset(0, 2, 3.5F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void render(@Nonnull AnimatorInsectoid animator, float delta, @Nonnull PoseStack matrixStack, @Nonnull VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        setAngles(this.wingLeft, this.wingRight, animator.getRotation(delta));
        this.wingLeft.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.wingRight.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
