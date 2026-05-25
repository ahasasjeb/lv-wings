package cc.lvjia.wings.client.flight;

import cc.lvjia.wings.util.function.FloatConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public interface FlightView {
    void ifFormPresent(Consumer<FormRenderer> consumer);

    void tick();

    void tickEyeHeight(float value, FloatConsumer valueOut);

    interface FormRenderer {
        @NonNull Identifier getTexture();

        @NonNull RenderType getRenderType();

        void render(@NonNull PoseStack matrixStack, @NonNull VertexConsumer buffer, int packedLight, int packedOverlay, float red,
                float green, float blue, float alpha, float delta);
    }
}
