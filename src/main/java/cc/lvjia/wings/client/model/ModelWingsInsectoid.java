package cc.lvjia.wings.client.model;

import cc.lvjia.wings.client.flight.AnimatorInsectoid;
import cc.lvjia.wings.client.flight.RotationAngles;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public final class ModelWingsInsectoid extends ModelWings<@NonNull AnimatorInsectoid> {
    private final @NonNull ModelPart wingLeft;
    private final @NonNull ModelPart wingRight;
    private final @NonNull RotationAngles rotation = new RotationAngles();

    public ModelWingsInsectoid(@NonNull ModelPart root) {
        super(root);
        this.wingLeft = Objects.requireNonNull(root.getChild("WingLeft"), "WingLeft");
        this.wingRight = Objects.requireNonNull(root.getChild("WingRight"), "WingRight");
    }

    public static @NonNull LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("WingLeft", CubeListBuilder.create().texOffs(0, 0).addBox(0, -8, 0, 19, 24, 0, new CubeDeformation(0.0F)), PartPose.offset(0, 2, 3.5F));

        partdefinition.addOrReplaceChild("WingRight", CubeListBuilder.create().texOffs(0, 24).addBox(-19, -8, 0, 19, 24, 0, new CubeDeformation(0.0F)), PartPose.offset(0, 2, 3.5F));

        return Objects.requireNonNull(LayerDefinition.create(meshdefinition, 64, 64), "insectoid wing layer");
    }

    @Override
    public void render(@NonNull AnimatorInsectoid animator, float delta, @NonNull PoseStack matrixStack,
                       @NonNull VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        animator.getRotation(delta, this.rotation);
        setAngles(this.wingLeft, this.wingRight, this.rotation);
        int color = ARGB.colorFromFloat(alpha, red, green, blue);
        this.wingLeft.render(matrixStack, buffer, packedLight, packedOverlay, color);
        this.wingRight.render(matrixStack, buffer, packedLight, packedOverlay, color);
    }
}
