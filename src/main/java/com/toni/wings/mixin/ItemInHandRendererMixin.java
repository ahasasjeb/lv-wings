package com.toni.wings.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.toni.wings.server.asm.WingsHooksClient;
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
    protected abstract void renderPlayerArm(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float equipProgress, float swingProgress, HumanoidArm arm);

    @Inject(method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER))
    private void wings$renderEmptyOffhand(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equipProgress,
                                          PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        if (hand == InteractionHand.OFF_HAND && stack.isEmpty() && WingsHooksClient.shouldRenderEmptyOffhand(player, this.mainHandItem)) {
            HumanoidArm arm = player.getMainArm().getOpposite();
            this.renderPlayerArm(poseStack, bufferSource, packedLight, equipProgress, swingProgress, arm);
        }
    }
}
