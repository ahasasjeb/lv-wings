package cc.lvjia.wings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerCapeModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

@SuppressWarnings("null")
public final class FabricLayerCapeWings extends RenderLayer<AvatarRenderState, PlayerModel> {

    private final @NonNull HumanoidModel<AvatarRenderState> model;

    public FabricLayerCapeWings(RenderLayerParent<AvatarRenderState, PlayerModel> parent, EntityModelSet entityModelSet) {
        super(Objects.requireNonNull(parent, "parent"));
        this.model = new PlayerCapeModel(
                Objects.requireNonNull(entityModelSet, "model set").bakeLayer(ModelLayers.PLAYER_CAPE));
    }

    @Override
    public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, int packedLight,
                       @NonNull AvatarRenderState state, float limbSwing, float limbSwingAmount) {
        WingsLayerRenderer.submitCape(poseStack, submitNodeCollector, packedLight, state, this.model);
    }
}
