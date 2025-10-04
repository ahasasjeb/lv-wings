package com.toni.wings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.toni.wings.WingsMod;
import com.toni.wings.client.flight.FlightViews;
import com.toni.wings.server.flight.Flights;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerCapeModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

public class LayerCapeWings extends RenderLayer<PlayerRenderState, PlayerModel> {

    private final HumanoidModel<PlayerRenderState> model;

    public LayerCapeWings(RenderLayerParent<PlayerRenderState, PlayerModel> parent, EntityModelSet entityModelSet) {
        super(parent);
        this.model = new PlayerCapeModel<>(entityModelSet.bakeLayer(ModelLayers.PLAYER_CAPE));
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int packedLight, @Nonnull PlayerRenderState state, float limbSwing, float limbSwingAmount) {
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
        ResourceLocation capeTexture = skin.capeTexture();
        if (capeTexture == null) {
            return;
        }

        poseStack.pushPose();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entitySolid(capeTexture));
        PlayerModel parentModel = (PlayerModel) this.getParentModel();
        parentModel.copyPropertiesTo(this.model);
        this.model.setupAnim(state);
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private boolean hasVisibleWings(AbstractClientPlayer player) {
        AtomicBoolean hasWings = new AtomicBoolean(false);
        FlightViews.get(player).ifPresent(flight -> flight.ifFormPresent(form -> hasWings.set(true)));
        if (hasWings.get()) {
            return true;
        }
        Flights.get(player).ifPresent(flight -> {
            if (flight.getWing() != WingsMod.NONE && flight.getWing() != WingsMod.WINGLESS) {
                hasWings.set(true);
            }
        });
        return hasWings.get();
    }
}
