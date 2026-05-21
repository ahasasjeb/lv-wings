package cc.lvjia.wings.client.renderer;

import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.server.apparatus.FlightApparatus;
import cc.lvjia.wings.server.flight.Flights;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerCapeModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

@SuppressWarnings("null")
public final class LayerCapeWings extends RenderLayer<AvatarRenderState, PlayerModel> {

    private final @NonNull HumanoidModel<AvatarRenderState> model;

    public LayerCapeWings(RenderLayerParent<AvatarRenderState, PlayerModel> parent, EntityModelSet entityModelSet) {
        super(Objects.requireNonNull(parent, "parent"));
        this.model = new PlayerCapeModel(
                Objects.requireNonNull(entityModelSet, "model set").bakeLayer(ModelLayers.PLAYER_CAPE));
    }

    @Override
    public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, int packedLight,
            @NonNull AvatarRenderState state, float limbSwing, float limbSwingAmount) {
        if (state.isInvisible || !state.showCape) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        if (!(level.getEntity(state.id) instanceof AbstractClientPlayer player)) {
            return;
        }

        if (hasVisibleWings(player)) {
            return;
        }

        PlayerSkin skin = state.skin;
        ClientAsset.Texture capeTexture = skin.cape();
        if (capeTexture == null) {
            return;
        }

        poseStack.pushPose();
        submitNodeCollector.submitModel(
                this.model,
                state,
                poseStack,
                RenderTypes.entitySolid(capeTexture.texturePath()),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null);
        poseStack.popPose();
    }

    private boolean hasVisibleWings(AbstractClientPlayer player) {
        FlightApparatus wing = Flights.get(player).getWing();
        return wing != WingsMod.NONE && wing != WingsMod.WINGLESS;
    }
}
