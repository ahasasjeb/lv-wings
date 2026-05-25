package cc.lvjia.wings.mixin;

import cc.lvjia.wings.server.potion.WingsBrewingRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionBrewing.class)
public abstract class PotionBrewingMixin {
    @Inject(method = "isIngredient(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void wings$isIngredient(ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (WingsBrewingRecipes.isIngredient(ingredient)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasMix(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void wings$hasMix(ItemStack source, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (WingsBrewingRecipes.hasMix(source, ingredient)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mix(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void wings$mix(ItemStack ingredient, ItemStack source, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = WingsBrewingRecipes.mix(ingredient, source);
        if (!result.isEmpty()) {
            cir.setReturnValue(result);
        }
    }
}
