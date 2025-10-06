package com.toni.wings.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.toni.wings.server.asm.WingsHooksClient;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V",
            at = @At("HEAD"))
    private void wings$trackRenderState(AbstractClientPlayer player, PlayerRenderState state, float partialTick, CallbackInfo ci) {
        WingsHooksClient.onExtractPlayerRenderState(player, state);
    }

    @Inject(method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
            at = @At("RETURN"))
    private void wings$applyRotations(PlayerRenderState state, PoseStack poseStack, float ageInTicks, float rotationYaw, CallbackInfo ci) {
        WingsHooksClient.onApplyPlayerRotations(state, poseStack);
    }
}
