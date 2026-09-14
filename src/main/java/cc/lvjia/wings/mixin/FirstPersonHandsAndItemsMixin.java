package cc.lvjia.wings.mixin;

import cc.lvjia.wings.client.hooks.EmptyOffhandRenderState;
import cc.lvjia.wings.client.hooks.ItemInHandHookSupport;
import cc.lvjia.wings.client.hooks.WingsHooksClient;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItems.class)
public abstract class FirstPersonHandsAndItemsMixin {
    @Shadow
    private ItemStack mainHandItem;
    @Shadow
    private ItemStack offHandItem;
    @Shadow
    private float offHandHeight;

    // 前两个 clamp 处理双手忙碌，第三个处理主手，第四个才是副手换装。
    @Redirect(method = "tick(Lnet/minecraft/client/player/LocalPlayer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F", ordinal = 3))
    private float wings$animateEmptyOffhand(float value, float min, float max, LocalPlayer player) {
        if (ItemInHandHookSupport.canRenderEmptyOffhand(player.getOffhandItem(), player.getMainHandItem())) {
            boolean lowerHand = WingsHooksClient.onCheckDoReequipAnimation(
                    this.offHandItem, player.getOffhandItem(), -1, this.mainHandItem);
            return Mth.clamp((lowerHand ? 0.0F : 1.0F) - this.offHandHeight, min, max);
        }
        return Mth.clamp(value, min, max);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void wings$extractEmptyOffhand(LocalPlayer player, float partialTicks,
                                          FirstPersonHandsAndItemsRenderState state, CallbackInfo ci) {
        // 非飞行时手臂高度会平滑降至零；保留提交行为，使起降动画连续。
        ((EmptyOffhandRenderState) state).wings$setRenderEmptyOffhand(
                ItemInHandHookSupport.canRenderEmptyOffhand(state.offHandItem, state.mainHandItem));
    }
}
