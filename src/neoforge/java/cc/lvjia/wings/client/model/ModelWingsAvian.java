package cc.lvjia.wings.client.model;

import cc.lvjia.wings.client.flight.AnimatorAvian;
import cc.lvjia.wings.client.flight.RotationAngles;
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
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("null")
public final class ModelWingsAvian extends ModelWings<@NonNull AnimatorAvian> {
        private static final @NonNull Field CUBES_FIELD = findCubesField();

        private final ImmutableList<@NonNull ModelPart> bonesLeft;
        private final ImmutableList<@NonNull ModelPart> bonesRight;
        private final ImmutableList<@NonNull ModelPart> feathersLeft;
        private final ImmutableList<@NonNull ModelPart> feathersRight;

        private final @NonNull ModelPart coracoidLeft;
        private final @NonNull ModelPart humerusLeft;
        private final @NonNull ModelPart ulnaLeft;
        private final @NonNull ModelPart carpalsLeft;
        private final @NonNull ModelPart coracoidRight;
        private final @NonNull ModelPart humerusRight;
        private final @NonNull ModelPart ulnaRight;
        private final @NonNull ModelPart carpalsRight;
        private final @NonNull RotationAngles rotation = new RotationAngles();

        public ModelWingsAvian(@NonNull ModelPart root) {
                super(root);

                this.coracoidLeft = Objects.requireNonNull(root.getChild("coracoidLeft"), "coracoidLeft");
                this.humerusLeft = Objects.requireNonNull(this.coracoidLeft.getChild("humerusLeft"), "humerusLeft");
                this.ulnaLeft = Objects.requireNonNull(this.humerusLeft.getChild("ulnaLeft"), "ulnaLeft");
                this.carpalsLeft = Objects.requireNonNull(this.ulnaLeft.getChild("carpalsLeft"), "carpalsLeft");

                this.coracoidRight = Objects.requireNonNull(root.getChild("coracoidRight"), "coracoidRight");
                this.humerusRight = Objects.requireNonNull(this.coracoidRight.getChild("humerusRight"), "humerusRight");
                this.ulnaRight = Objects.requireNonNull(this.humerusRight.getChild("ulnaRight"), "ulnaRight");
                this.carpalsRight = Objects.requireNonNull(this.ulnaRight.getChild("carpalsRight"), "carpalsRight");

                ModelPart feathersCoracoidLeft = Objects.requireNonNull(
                                this.coracoidLeft.getChild("feathersCoracoidLeft"), "feathersCoracoidLeft");
                add3DTexture(feathersCoracoidLeft, 6, 40, 0, 0, -1, 6, 8);
                ModelPart feathersTertiaryLeft = Objects.requireNonNull(
                                this.humerusLeft.getChild("feathersTertiaryLeft"), "feathersTertiaryLeft");
                add3DTexture(feathersTertiaryLeft, 10, 14, 0, 0, -0.5F, 10, 14);
                ModelPart feathersSecondaryLeft = Objects.requireNonNull(
                                this.ulnaLeft.getChild("feathersSecondaryLeft"), "feathersSecondaryLeft");
                add3DTexture(feathersSecondaryLeft, 31, 14, -2, 0, -0.5F, 11, 12);
                ModelPart feathersPrimaryLeft = Objects.requireNonNull(
                                this.carpalsLeft.getChild("feathersPrimaryLeft"), "feathersPrimaryLeft");
                add3DTexture(feathersPrimaryLeft, 53, 14, 0, -2.1F, -0.5F, 11, 11);

                ModelPart feathersCoracoidRight = Objects.requireNonNull(
                                this.coracoidRight.getChild("feathersCoracoidRight"), "feathersCoracoidRight");
                add3DTexture(feathersCoracoidRight, 0, 40, -6, 0, -1, 6, 8);
                ModelPart feathersTertiaryRight = Objects.requireNonNull(
                                this.humerusRight.getChild("feathersTertiaryRight"), "feathersTertiaryRight");
                add3DTexture(feathersTertiaryRight, 0, 14, -10, 0, -0.5F, 10, 14);
                ModelPart feathersSecondaryRight = Objects.requireNonNull(
                                this.ulnaRight.getChild("feathersSecondaryRight"), "feathersSecondaryRight");
                add3DTexture(feathersSecondaryRight, 20, 14, -9, 0, -0.5F, 11, 12);
                ModelPart feathersPrimaryRight = Objects.requireNonNull(
                                this.carpalsRight.getChild("feathersPrimaryRight"), "feathersPrimaryRight");
                add3DTexture(feathersPrimaryRight, 42, 14, -11, -2.1F, -0.5F, 11, 11);

                this.bonesLeft = ImmutableList.of(this.coracoidLeft, this.humerusLeft, this.ulnaLeft, this.carpalsLeft);
                this.bonesRight = ImmutableList.of(this.coracoidRight, this.humerusRight, this.ulnaRight,
                                this.carpalsRight);

                this.feathersLeft = ImmutableList.of(
                                feathersCoracoidLeft, feathersTertiaryLeft,
                                feathersSecondaryLeft, feathersPrimaryLeft);

                this.feathersRight = ImmutableList.of(
                                feathersCoracoidRight, feathersTertiaryRight,
                                feathersSecondaryRight, feathersPrimaryRight);

        }

        public static @NonNull LayerDefinition createBodyLayer() {
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

                return Objects.requireNonNull(LayerDefinition.create(meshdefinition, 64, 64), "avian wing layer");
        }

        private static void add3DTexture(
                        @NonNull ModelPart model,
                        int u, int v,
                        float offX, float offY, float offZ,
                        int width, int height) {
                getCubes(model).add(Model3DTexture.create(offX, offY, offZ, width, height, u, v, 64, 64));

        }

        private static @NonNull Field findCubesField() {
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
        private static @NonNull List<ModelPart.@NonNull Cube> getCubes(@NonNull ModelPart part) {
                try {
                        List<ModelPart.@NonNull Cube> cubes = Objects.requireNonNull(
                                        (List<ModelPart.Cube>) CUBES_FIELD.get(part), "model part cubes");
                        List<ModelPart.@NonNull Cube> mutable = new ArrayList<>(cubes);
                        CUBES_FIELD.set(part, mutable);
                        return mutable;
                } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to access ModelPart cubes list via reflection", e);
                }
        }

        @Override
        public void render(@NonNull AnimatorAvian animator, float delta, @NonNull PoseStack matrixStack,
                        @NonNull VertexConsumer buffer,
                        int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
                int color = ARGB.colorFromFloat(alpha, red, green, blue);

                for (int i = 0; i < this.bonesLeft.size(); i++) {
                        ModelPart left = Objects.requireNonNull(this.bonesLeft.get(i), "left bone");
                        ModelPart right = Objects.requireNonNull(this.bonesRight.get(i), "right bone");
                        animator.getWingRotation(i, delta, this.rotation);
                        setAngles(left, right, this.rotation);
                }
                for (int i = 0; i < this.feathersLeft.size(); i++) {
                        ModelPart left = Objects.requireNonNull(this.feathersLeft.get(i), "left feather");
                        ModelPart right = Objects.requireNonNull(this.feathersRight.get(i), "right feather");
                        animator.getFeatherRotation(i, delta, this.rotation);
                        setAngles(left, right, this.rotation);
                }

                this.coracoidLeft.render(matrixStack, buffer, packedLight, packedOverlay, color);
                this.coracoidRight.render(matrixStack, buffer, packedLight, packedOverlay, color);

        }
}
