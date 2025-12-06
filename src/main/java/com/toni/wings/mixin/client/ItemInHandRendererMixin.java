package com.toni.wings.mixin.client;

import com.toni.wings.server.asm.WingsHooksClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemInHandRenderer.class)
abstract class ItemInHandRendererMixin {
    @Shadow
    private ItemStack mainHandItem;

    @Shadow
    protected abstract void renderPlayerArm(PoseStack poseStack, MultiBufferSource buffer, int packedLight, float equipProgress, float swingProgress, HumanoidArm arm);

    @Inject(method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0, shift = At.Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILHARD)
    private void wings$renderEmptyOffhand(AbstractClientPlayer player, float tickDelta, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equipProgress,
                                           PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci, boolean isMainHand, HumanoidArm arm) {
        if (!isMainHand && stack.isEmpty() && !player.isInvisible() && WingsHooksClient.onCheckRenderEmptyHand(false, this.mainHandItem)) {
            this.renderPlayerArm(poseStack, buffer, packedLight, equipProgress, swingProgress, arm);
        }
    }
}
