package cc.lvjia.wings.client.model;

import cc.lvjia.wings.client.flight.Animator;
import cc.lvjia.wings.client.flight.RotationAngles;
import cc.lvjia.wings.util.MathH;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("null")
public abstract class ModelWings<A extends @NonNull Animator> extends Model<A> {
    protected ModelWings(@NonNull ModelPart root) {
        super(root, RenderTypes::entityCutout);
    }

    static void setAngles(@NonNull ModelPart left, @NonNull ModelPart right, @NonNull RotationAngles angles) {
        right.xRot = (left.xRot = MathH.toRadians(angles.x()));
        right.yRot = -(left.yRot = MathH.toRadians(angles.y()));
        right.zRot = -(left.zRot = MathH.toRadians(angles.z()));
    }

    public abstract void render(@NonNull A animator, float delta, @NonNull PoseStack matrixStack,
                                @NonNull VertexConsumer buffer, int packedLight,
                                int packedOverlay, float red, float green, float blue, float alpha);
}
