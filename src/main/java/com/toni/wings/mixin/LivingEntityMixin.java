package com.toni.wings.mixin;

import com.toni.wings.server.asm.WingsHooks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity {
    private LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(method = "tickHeadTurn(FF)F", at = @At("HEAD"), cancellable = true)
    private void wings$useCustomBodyRotation(float bodyYaw, float headYaw, CallbackInfoReturnable<Float> cir) {
        if (WingsHooks.onUpdateBodyRotation((LivingEntity) (Object) this, bodyYaw)) {
            cir.setReturnValue(0.0F);
        }
    }

    @Redirect(method = "isVisuallySwimming()Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isFallFlying()Z"))
    private boolean wings$wrapFlightSwimming(LivingEntity instance) {
        return WingsHooks.onFlightCheck(instance, instance.isFallFlying());
    }
}
