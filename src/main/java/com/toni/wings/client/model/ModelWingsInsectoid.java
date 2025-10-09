package com.toni.wings.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.toni.wings.client.flight.AnimatorInsectoid;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.ARGB;

public final class ModelWingsInsectoid extends ModelWings<AnimatorInsectoid> {
    //private final ModelPart root;

    private final ModelPart wingLeft;

   private final ModelPart wingRight;

    public ModelWingsInsectoid(ModelPart root) {
        super(root);

        //this.root = root;
        this.wingLeft = root.getChild("WingLeft");
        this.wingRight = root.getChild("WingRight");

        /*this.texWidth = this.texHeight = 64;
        this.root = new ModelPart(this, 0, 0);
        this.wingLeft = new ModelPart(this, 0, 0);
        this.wingLeft.setPos(0, 2, 3.5F);
        this.wingLeft.addBox(0, -8, 0, 19, 24, 0, 0);
        this.wingRight = new ModelPart(this, 0, 24);
        this.wingRight.setPos(0, 2, 3.5F);
        this.wingRight.addBox(-19, -8, 0, 19, 24, 0, 0);
        this.root.addChild(this.wingLeft);
        this.root.addChild(this.wingRight);*/
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("WingLeft", CubeListBuilder.create().texOffs(0, 0).addBox(0, -8, 0, 19, 24, 0, new CubeDeformation(0.0F)), PartPose.offset(0, 2, 3.5F));

        partdefinition.addOrReplaceChild("WingRight", CubeListBuilder.create().texOffs(0, 24).addBox(-19, -8, 0, 19, 24, 0, new CubeDeformation(0.0F)), PartPose.offset(0, 2, 3.5F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void render(AnimatorInsectoid animator, float delta, PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        setAngles(this.wingLeft, this.wingRight, animator.getRotation(delta));
        int color = ARGB.colorFromFloat(alpha, red, green, blue);
        this.wingLeft.render(matrixStack, buffer, packedLight, packedOverlay, color);
        this.wingRight.render(matrixStack, buffer, packedLight, packedOverlay, color);
    }
}
