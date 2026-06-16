package cc.lvjia.wings.mixin;

import cc.lvjia.wings.client.hooks.WingsHooksClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修改手持物品渲染器，支持翅膀飞行时的空手渲染
 */
@Mixin(ItemInHandRenderer.class)
public abstract class FabricItemInHandRendererMixin {
    private static final String SUBMIT_ARM_WITH_ITEM = "submitArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V";
    @Shadow
    private ItemStack mainHandItem;

    @Shadow
    private ItemStack offHandItem;

    @Shadow
    private float offHandHeight;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private void renderPlayerArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight,
                                 float inverseArmHeight, float swingProgress, HumanoidArm arm) {
    }

    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F", ordinal = 3))
    private float wings$applyEmptyOffhandSwapAnimation(float value, float min, float max) {
        LocalPlayer player = this.minecraft.player;
        if (player != null
                && WingsHooksClient.canRenderEmptyOffhand(player.getOffhandItem(), player.getMainHandItem())) {
            boolean swapAnimOff = WingsHooksClient.onCheckDoReequipAnimation(this.offHandItem, player.getOffhandItem(),
                    -1);
            float offHandTargetHeight = swapAnimOff ? 0.0F : 1.0F;
            return Mth.clamp(offHandTargetHeight - this.offHandHeight, min, max);
        }
        return Mth.clamp(value, min, max);
    }

    /**
     * 26.2 仅在空手分支内渲染副手手臂。不能修改 isMainHand：该标志还决定 humanoidArm 与物品渲染路径。
     * 旧版改的是独立的 stack-empty 布尔值；此处等价于在空手副手时补一次 renderPlayerArm。
     */
    @Inject(
            method = SUBMIT_ARM_WITH_ITEM,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void wings$renderAllowedEmptyOffhandArm(AbstractClientPlayer player, float frameInterp, float xRot,
                                                      InteractionHand hand, float attack, ItemStack itemStack,
                                                      float inverseArmHeight, PoseStack poseStack,
                                                      SubmitNodeCollector submitNodeCollector, int lightCoords,
                                                      CallbackInfo ci) {
        if (!itemStack.isEmpty() || hand != InteractionHand.OFF_HAND || player.isInvisible()) {
            return;
        }
        if (!WingsHooksClient.onCheckRenderEmptyHand(false, player, hand, itemStack, this.mainHandItem)) {
            return;
        }
        this.renderPlayerArm(poseStack, submitNodeCollector, lightCoords, inverseArmHeight, attack,
                player.getMainArm().getOpposite());
    }
}