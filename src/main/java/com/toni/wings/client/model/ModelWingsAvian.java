package com.toni.wings.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.toni.wings.client.flight.AnimatorAvian;
import com.toni.wings.util.MathH;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public final class ModelWingsAvian extends ModelWings<AnimatorAvian> {
        private static final Field CUBES_FIELD = findCubesField();

    private ImmutableList<ModelPart> bonesLeft, bonesRight;
    private ImmutableList<ModelPart> feathersLeft, feathersRight;

    private ModelPart coracoidLeft;
    private ModelPart humerusLeft;
    private ModelPart ulnaLeft;
    private ModelPart carpalsLeft;
    private ModelPart coracoidRight;
    private ModelPart humerusRight;
    private ModelPart ulnaRight;
    private ModelPart carpalsRight;
    private ModelPart feathersCoracoidLeft;
    private ModelPart feathersPrimaryLeft;
    private ModelPart feathersSecondaryLeft;
    private ModelPart feathersTertiaryLeft;
    private ModelPart feathersCoracoidRight;
    private ModelPart feathersPrimaryRight;
    private ModelPart feathersSecondaryRight;
    private ModelPart feathersTertiaryRight;




        public ModelWingsAvian(ModelPart root) {
                super(root);

        this.coracoidLeft = root.getChild("coracoidLeft");
        this.humerusLeft = coracoidLeft.getChild("humerusLeft");
        this.ulnaLeft = humerusLeft.getChild("ulnaLeft");
        this.carpalsLeft = ulnaLeft.getChild("carpalsLeft");

        this.coracoidRight = root.getChild("coracoidRight");
        this.humerusRight = coracoidRight.getChild("humerusRight");
        this.ulnaRight = humerusRight.getChild("ulnaRight");
        this.carpalsRight = ulnaRight.getChild("carpalsRight");

        this.feathersCoracoidLeft = coracoidLeft.getChild("feathersCoracoidLeft");
        //this.feathersCoracoidLeft = new ModelPart(new ArrayList<>(ModelPart.Cube()))
        add3DTexture(this.feathersCoracoidLeft, 6, 40, 0, 0, -1, 6, 8);
        this.feathersTertiaryLeft = humerusLeft.getChild("feathersTertiaryLeft");
        add3DTexture(this.feathersTertiaryLeft, 10, 14, 0, 0, -0.5F, 10, 14);
        this.feathersSecondaryLeft = ulnaLeft.getChild("feathersSecondaryLeft");
        add3DTexture(this.feathersSecondaryLeft, 31, 14, -2, 0, -0.5F, 11, 12);
        this.feathersPrimaryLeft = carpalsLeft.getChild("feathersPrimaryLeft");
        add3DTexture(this.feathersPrimaryLeft, 53, 14, 0, -2.1F, -0.5F, 11, 11);

        this.feathersCoracoidRight = coracoidRight.getChild("feathersCoracoidRight");
        add3DTexture(this.feathersCoracoidRight, 0, 40, -6, 0, -1, 6, 8);
        this.feathersTertiaryRight = humerusRight.getChild("feathersTertiaryRight");
        add3DTexture(this.feathersTertiaryRight, 0, 14, -10, 0, -0.5F, 10, 14);
        this.feathersSecondaryRight = ulnaRight.getChild("feathersSecondaryRight");
        add3DTexture(this.feathersSecondaryRight, 20, 14, -9, 0, -0.5F, 11, 12);
        this.feathersPrimaryRight = carpalsRight.getChild("feathersPrimaryRight");
        add3DTexture(this.feathersPrimaryRight, 42, 14, -11, -2.1F, -0.5F, 11, 11);

        this.bonesLeft = ImmutableList.of(this.coracoidLeft, this.humerusLeft, this.ulnaLeft, this.carpalsLeft);
        this.bonesRight = ImmutableList.of(this.coracoidRight, this.humerusRight, this.ulnaRight, this.carpalsRight);

        this.feathersLeft = ImmutableList.of(
                this.feathersCoracoidLeft, this.feathersTertiaryLeft,
                this.feathersSecondaryLeft, this.feathersPrimaryLeft
        );

        this.feathersRight = ImmutableList.of(
                this.feathersCoracoidRight, this.feathersTertiaryRight,
                this.feathersSecondaryRight, this.feathersPrimaryRight
        );

    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();

        PartDefinition coracoidLeft = root.addOrReplaceChild("coracoidLeft", CubeListBuilder.create()
                .texOffs(0, 28)
                .addBox(0, -1.5F, -1.5F, 5, 3, 3, new CubeDeformation(0.0F)),
                PartPose.offset(1.5F, 5.5F, 2.5F));

        PartDefinition coracoidRight = root.addOrReplaceChild("coracoidRight", CubeListBuilder.create()
                        .texOffs(0, 34)
                        .addBox(-5, -1.5F, -1.5F, 5, 3, 3, new CubeDeformation(0.0F)),
                PartPose.offset(-1.5F, 5.5F, 2.5F));

        PartDefinition humerusLeft = coracoidLeft.addOrReplaceChild("humerusLeft", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-0.1F, -1.1F, -2, 7, 3, 4, new CubeDeformation(0.0F)),
                PartPose.offset(4.7F, -0.6F, 0.1F));

        PartDefinition humerusRight = coracoidRight.addOrReplaceChild("humerusRight", CubeListBuilder.create()
                        .texOffs(0, 7)
                        .addBox(-6.9F, -1.1F, -2, 7, 3, 4, new CubeDeformation(0.0F)),
                PartPose.offset(-4.7F, -0.6F, 0.1F));

        PartDefinition ulnaLeft = humerusLeft.addOrReplaceChild("ulnaLeft", CubeListBuilder.create()
                        .texOffs(22, 0)
                        .addBox(0, -1.5F, -1.5F, 9, 3, 3, new CubeDeformation(0.0F)),
                PartPose.offset(6.5F, 0.2F, 0.1F));

        PartDefinition ulnaRight = humerusRight.addOrReplaceChild("ulnaRight", CubeListBuilder.create()
                        .texOffs(22, 6)
                        .addBox(-9, -1.5F, -1.5F, 9, 3, 3, new CubeDeformation(0.0F)),
                PartPose.offset(-6.5F, 0.2F, 0.1F));

        PartDefinition carpalsLeft = ulnaLeft.addOrReplaceChild("carpalsLeft", CubeListBuilder.create()
                        .texOffs(22, 0)
                        .addBox(0, -1, -1, 5, 2, 2, new CubeDeformation(0.0F)),
                PartPose.offset(8.5F, 0, 0));

        PartDefinition carpalsRight = ulnaRight.addOrReplaceChild("carpalsRight", CubeListBuilder.create()
                        .texOffs(22, 0)
                        .addBox(-5, -1, -1, 5, 2, 2, new CubeDeformation(0.0F)),
                PartPose.offset(-8.5F, 0, 0));

        coracoidLeft.addOrReplaceChild("feathersCoracoidLeft", CubeListBuilder.create(),
                PartPose.offset(0.4F, 0, 1));

        coracoidRight.addOrReplaceChild("feathersCoracoidRight", CubeListBuilder.create(),
                PartPose.offset(-0.4F, 0, 1));

        humerusLeft.addOrReplaceChild("feathersTertiaryLeft", CubeListBuilder.create(),
                PartPose.offset(0, 1.5F, 1));

        humerusRight.addOrReplaceChild("feathersTertiaryRight", CubeListBuilder.create(),
                PartPose.offset(0, 1.5F, 1));

        ulnaLeft.addOrReplaceChild("feathersSecondaryLeft", CubeListBuilder.create(),
                PartPose.offset(0, 1, 0));

        ulnaRight.addOrReplaceChild("feathersSecondaryRight", CubeListBuilder.create(),
                PartPose.offset(0, 1, 0));

        carpalsLeft.addOrReplaceChild("feathersPrimaryLeft", CubeListBuilder.create(),
                PartPose.offset(0, 0, 0));

        carpalsRight.addOrReplaceChild("feathersPrimaryRight", CubeListBuilder.create(),
                PartPose.offset(0, 0, 0));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void render(AnimatorAvian animator, float delta, PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
                int color = ((int)(alpha * 255) << 24) | ((int)(red * 255) << 16) | ((int)(green * 255) << 8) | (int)(blue * 255);

        for (int i = 0; i < this.bonesLeft.size(); i++) {
            ModelPart left = this.bonesLeft.get(i);
            ModelPart right = this.bonesRight.get(i);
            setAngles(left, right, animator.getWingRotation(i, delta));
        }
        for (int i = 0; i < this.feathersLeft.size(); i++) {
            ModelPart left = this.feathersLeft.get(i);
            ModelPart right = this.feathersRight.get(i);
            setAngles(left, right, animator.getFeatherRotation(i, delta));
        }

                this.coracoidLeft.render(matrixStack, buffer, packedLight, packedOverlay, color);
                this.coracoidRight.render(matrixStack, buffer, packedLight, packedOverlay, color);

    }

    @Override
        public void renderToBuffer(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        // Since the mod uses custom render method, this is not used, but implement to satisfy abstract method
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    static void setAngles(ModelPart left, ModelPart right, Vec3 angles) {
        right.xRot = (left.xRot = MathH.toRadians((float) angles.x));
        right.yRot = -(left.yRot = MathH.toRadians((float) angles.y));
        right.zRot = -(left.zRot = MathH.toRadians((float) angles.z));
    }

    private static void add3DTexture(
        ModelPart model,
        int u, int v,
        float offX, float offY, float offZ,
        int width, int height
    ) {
        getCubes(model).add(Model3DTexture.create(offX, offY, offZ, width, height, u, v, 64, 64));

    }

        private static Field findCubesField() {
                for (Field field : ModelPart.class.getDeclaredFields()) {
                        if (Modifier.isStatic(field.getModifiers())) {
                                continue;
                        }
                        if (List.class.isAssignableFrom(field.getType())) {
                                field.setAccessible(true);
                                return field;
                        }
                }
                throw new IllegalStateException("Unable to locate ModelPart cubes field");
        }

        @SuppressWarnings("unchecked")
        private static List<ModelPart.Cube> getCubes(ModelPart part) {
                try {
                        List<ModelPart.Cube> cubes = (List<ModelPart.Cube>) CUBES_FIELD.get(part);
                        List<ModelPart.Cube> mutable = new ArrayList<>(cubes);
                        CUBES_FIELD.set(part, mutable);
                        return mutable;
                } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                }
        }
}
