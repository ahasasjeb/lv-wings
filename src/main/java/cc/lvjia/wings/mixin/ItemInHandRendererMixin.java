package cc.lvjia.wings.mixin;

import cc.lvjia.wings.server.asm.WingsHooksClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow
    private ItemStack mainHandItem;

    @Unique
    private boolean wings$forceOppositeArm;

    @Unique
    private InteractionHand wings$currentHand;

    @Inject(method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
            at = @At("HEAD"))
    private void wings$cacheHand(AbstractClientPlayer player, float f, float f1, InteractionHand hand, float f2, ItemStack stack, float f3, PoseStack poseStack, SubmitNodeCollector collector, int light, CallbackInfo ci) {
        this.wings$currentHand = hand;
        this.wings$forceOppositeArm = false;
    }

    @ModifyVariable(method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
                    at = @At(value = "STORE"), ordinal = 0)
    private boolean wings$allowEmptyOffhandRender(boolean original) {
        boolean allowed = WingsHooksClient.onCheckRenderEmptyHand(original, this.mainHandItem);
        this.wings$forceOppositeArm = !original && allowed && this.wings$currentHand == InteractionHand.OFF_HAND;
        return allowed;
    }

    // The first STORE following the boolean flag writes the local HumanoidArm in 1.21.9.
    @ModifyVariable(method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
                    at = @At(value = "STORE"), ordinal = 0)
    private HumanoidArm wings$restoreOffhandArm(HumanoidArm arm, AbstractClientPlayer player, float f, float f1, InteractionHand hand, float f2, ItemStack stack, float f3, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        if (this.wings$forceOppositeArm && hand == InteractionHand.OFF_HAND) {
            return player.getMainArm().getOpposite();
        }
        return arm;
    }
}
