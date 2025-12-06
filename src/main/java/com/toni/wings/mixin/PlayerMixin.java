package com.toni.wings.mixin;

import com.toni.wings.server.asm.WingsHooks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
abstract class PlayerMixin extends LivingEntity {
    private PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Redirect(method = "updatePlayerPose", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isFallFlying()Z"))
    private boolean wings$wrapUpdatePose(Player instance) {
        return WingsHooks.onFlightCheck(instance, instance.isFallFlying());
    }

    @Inject(method = "checkMovementStatistics(DDD)V", at = @At(value = "INVOKE", target = "Ljava/lang/Math;round(F)I", ordinal = 5, shift = At.Shift.AFTER))
    private void wings$onAddFlown(double x, double y, double z, CallbackInfo ci) {
        WingsHooks.onAddFlown((Player) (Object) this, x, y, z);
    }
}
