package com.toni.wings.mixin;

import com.toni.wings.server.asm.WingsHooks;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "tickHeadTurn(FF)F", at = @At("HEAD"), cancellable = true)
    private void wings$updateBodyRotation(float movementYaw, float delta, CallbackInfoReturnable<Float> cir) {
        if (WingsHooks.onUpdateBodyRotation((LivingEntity) (Object) this, movementYaw)) {
            cir.setReturnValue(0.0F);
        }
    }

    @Redirect(method = "isVisuallySwimming()Z",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isFallFlying()Z"))
    private boolean wings$overrideVisualSwim(LivingEntity instance) {
        boolean vanilla = instance.isFallFlying();
        return WingsHooks.onFlightCheck(instance, vanilla);
    }
}
