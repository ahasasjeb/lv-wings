package cc.lvjia.wings.client.model;

import cc.lvjia.wings.client.flight.AnimatorAvian;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.ARGB;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public final class ModelWingsAvian extends ModelWings<AnimatorAvian> {
    private static final Field CUBES_FIELD = findCubesField();

    private final ImmutableList<ModelPart> bonesLeft;
    private final ImmutableList<ModelPart> bonesRight;
    private final ImmutableList<ModelPart> feathersLeft;
    private final ImmutableList<ModelPart> feathersRight;

    private final ModelPart coracoidLeft;
    private final ModelPart humerusLeft;
    private final ModelPart ulnaLeft;
    private final ModelPart carpalsLeft;
    private final ModelPart coracoidRight;
    private final ModelPart humerusRight;
    private final ModelPart ulnaRight;
    private final ModelPart carpalsRight;
    private final ModelPart feathersCoracoidLeft;
    private final ModelPart feathersPrimaryLeft;
    private final ModelPart feathersSecondaryLeft;
    private final ModelPart feathersTertiaryLeft;
    private final ModelPart feathersCoracoidRight;
    private final ModelPart feathersPrimaryRight;
    private final ModelPart feathersSecondaryRight;
    private final ModelPart feathersTertiaryRight;


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
                        throw new RuntimeException("Failed to access ModelPart cubes list via reflection", e);
        }
    }

    @Override
    public void render(AnimatorAvian animator, float delta, PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        int color = ARGB.colorFromFloat(alpha, red, green, blue);

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
}
