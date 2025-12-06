package com.toni.wings.mixin.client;

import com.toni.wings.server.asm.WingsHooksClient;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ForgeHooksClient.class, remap = false)
abstract class ForgeHooksClientMixin {
    @Inject(method = "shouldCauseReequipAnimation", at = @At("HEAD"), cancellable = true)
    private static void wings$overrideReequip(ItemStack from, ItemStack to, int slot, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(WingsHooksClient.onCheckDoReequipAnimation(from, to, slot));
    }
}
