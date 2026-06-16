package cc.lvjia.wings.mixin;

import cc.lvjia.wings.client.hooks.WingsHooksClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修改手持物品渲染器，支持翅膀飞行时的空手渲染
 */
@Mixin(ItemInHandRenderer.class)
public abstract class NeoForgeItemInHandRendererMixin {
    private static final String SUBMIT_ARM_WITH_ITEM = "submitArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V";
    @Shadow
    private ItemStack mainHandItem;

    @Shadow
    private void renderPlayerArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight,
                                 float inverseArmHeight, float swingProgress, HumanoidArm arm) {
    }

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
        if (!WingsHooksClient.onCheckRenderEmptyHand(false, this.mainHandItem)) {
            return;
        }
        this.renderPlayerArm(poseStack, submitNodeCollector, lightCoords, inverseArmHeight, attack,
                player.getMainArm().getOpposite());
    }
}