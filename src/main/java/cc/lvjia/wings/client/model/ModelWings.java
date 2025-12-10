package cc.lvjia.wings.client.model;

import cc.lvjia.wings.client.flight.Animator;
import cc.lvjia.wings.util.MathH;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;


public abstract class ModelWings<A extends Animator> extends Model<A> {
    protected ModelWings(ModelPart root) {
        super(root, RenderTypes::entityCutout);
    }

    static void setAngles(ModelPart left, ModelPart right, Vec3 angles) {
        right.xRot = (left.xRot = MathH.toRadians((float) angles.x));
        right.yRot = -(left.yRot = MathH.toRadians((float) angles.y));
        right.zRot = -(left.zRot = MathH.toRadians((float) angles.z));
    }

    public abstract void render(A animator, float delta, PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha);
}
