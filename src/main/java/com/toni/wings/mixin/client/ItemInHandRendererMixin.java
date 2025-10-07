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

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow
    private ItemStack mainHandItem;

    @Shadow
    protected abstract void renderPlayerArm(PoseStack poseStack, MultiBufferSource buffer, int packedLight, float swingProgress, float equipProgress, HumanoidArm arm);

    @Inject(method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", ordinal = 0))
    private void wings$renderEmptyOffhand(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float equipProgress, ItemStack stack, float swingProgress, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (!stack.isEmpty()) {
            return;
        }
        if (hand != InteractionHand.OFF_HAND) {
            return;
        }
        if (player.isInvisible()) {
            return;
        }
        if (!WingsHooksClient.onCheckRenderEmptyHand(false, this.mainHandItem)) {
            return;
        }
        HumanoidArm arm = player.getMainArm().getOpposite();
        this.renderPlayerArm(poseStack, buffer, packedLight, swingProgress, equipProgress, arm);
    }
}
