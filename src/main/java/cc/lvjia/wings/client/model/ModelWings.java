package cc.lvjia.wings.client.model;

import cc.lvjia.wings.client.flight.Animator;
import cc.lvjia.wings.client.flight.RotationAngles;
import cc.lvjia.wings.util.MathH;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public abstract class ModelWings<A extends Animator> extends Model<A> {
    protected ModelWings(ModelPart root) {
        super(root, RenderTypes::entityCutout);
    }

    static void setAngles(ModelPart left, ModelPart right, RotationAngles angles) {
        right.xRot = (left.xRot = MathH.toRadians(angles.x()));
        right.yRot = -(left.yRot = MathH.toRadians(angles.y()));
        right.zRot = -(left.zRot = MathH.toRadians(angles.z()));
    }

    public abstract void render(A animator, float delta, PoseStack matrixStack, VertexConsumer buffer, int packedLight,
            int packedOverlay, float red, float green, float blue, float alpha);
}
