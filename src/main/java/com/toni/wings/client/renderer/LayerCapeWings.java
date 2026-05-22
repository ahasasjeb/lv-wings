package com.toni.wings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.toni.wings.WingsMod;
import com.toni.wings.client.flight.FlightView;
import com.toni.wings.client.flight.FlightViews;
import com.toni.wings.server.flight.Flights;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;

import javax.annotation.Nonnull;

public class LayerCapeWings extends CapeLayer {

    public LayerCapeWings(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int packedLight, @Nonnull AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (hasVisibleWings(player)) {
            return;
        }
        super.render(poseStack, buffer, packedLight, player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
    }

    private boolean hasVisibleWings(AbstractClientPlayer player) {
        if (FlightViews.get(player).filter(FlightView::hasForm).isPresent()) {
            return true;
        }
        return Flights.get(player)
            .filter(flight -> flight.getWing() != WingsMod.NONE && flight.getWing() != WingsMod.WINGLESS)
            .isPresent();
    }
}
