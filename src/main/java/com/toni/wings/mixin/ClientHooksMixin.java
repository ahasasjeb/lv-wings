package com.toni.wings.mixin;

import com.toni.wings.server.asm.WingsHooksClient;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientHooks.class)
public abstract class ClientHooksMixin {
    @Inject(method = "shouldCauseReequipAnimation(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;I)Z",
            at = @At("HEAD"), cancellable = true)
    private static void wings$overrideReequipAnimation(ItemStack from, ItemStack to, int slot, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(WingsHooksClient.onCheckDoReequipAnimation(from, to, slot));
    }
}
