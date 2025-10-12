package cc.lvjia.wings.mixin;

import cc.lvjia.wings.server.asm.WingsHooksClient;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修改客户端钩子，控制装备动画
 */
@Mixin(ClientHooks.class)
public abstract class ClientHooksMixin {
    /**
     * 覆盖重新装备动画检查，防止翅膀飞行时不必要的动画
     */
    @Inject(method = "shouldCauseReequipAnimation(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;I)Z",
            at = @At("HEAD"), cancellable = true)
    private static void wings$overrideReequipAnimation(ItemStack from, ItemStack to, int slot, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(WingsHooksClient.onCheckDoReequipAnimation(from, to, slot));
    }
}
