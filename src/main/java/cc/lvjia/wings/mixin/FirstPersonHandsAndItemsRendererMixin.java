package cc.lvjia.wings.mixin;

import cc.lvjia.wings.client.hooks.EmptyOffhandRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonHandsAndItemsRendererMixin {
    @Shadow
    private void renderPlayerArm(PoseStack poseStack, SubmitNodeCollector collector, int light,
                                 float inverseArmHeight, float attack, HumanoidArm arm, PlayerRenderState playerState) {
    }

    @Inject(method = "submitArmWithItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0))
    private void wings$renderEmptyOffhand(PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state,
                                          float partialTicks, float xRot, InteractionHand hand, float attack,
                                          ItemStack itemStack, float inverseArmHeight, PoseStack poseStack,
                                          SubmitNodeCollector collector, int light, CallbackInfo ci) {
        var avatar = playerState.avatarRenderState;
        if (hand == InteractionHand.OFF_HAND && itemStack.isEmpty() && avatar != null && !avatar.isInvisible
                && ((EmptyOffhandRenderState) state).wings$renderEmptyOffhand()) {
            this.renderPlayerArm(poseStack, collector, light, inverseArmHeight, attack,
                    avatar.mainArm.getOpposite(), playerState);
        }
    }
}
