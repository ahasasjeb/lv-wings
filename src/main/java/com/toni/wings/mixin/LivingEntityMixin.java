package com.toni.wings.mixin;

import com.toni.wings.server.asm.WingsHooks;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "tickHeadTurn(F)V", at = @At("HEAD"), cancellable = true)
    private void wings$cancelTickHeadTurn(float movementYaw, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (WingsHooks.onUpdateBodyRotation(self, movementYaw)) {
            ci.cancel();
        }
    }

    @Redirect(method = "isVisuallySwimming()Z",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isFallFlying()Z"))
    private boolean wings$redirectVisualSwimmingFlightCheck(LivingEntity instance) {
        return WingsHooks.onFlightCheck(instance, instance.isFallFlying());
    }
}
