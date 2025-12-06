package com.toni.wings.mixin.client;

import com.toni.wings.server.asm.WingsHooksClient;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
abstract class PlayerModelMixin<T extends LivingEntity> {
    @ModifyVariable(method = "setupAnim", at = @At("HEAD"), ordinal = 0)
    private float wings$modifyLimbSwing(float limbSwing, LivingEntity living) {
        if (living.getPose() == Pose.FALL_FLYING) {
            return 0.0F;
        }
        return limbSwing;
    }

    @ModifyVariable(method = "setupAnim", at = @At("HEAD"), ordinal = 1)
    private float wings$modifyLimbSwingAmount(float limbSwingAmount, LivingEntity living) {
        if (living.getPose() == Pose.FALL_FLYING) {
            return 0.0F;
        }
        return limbSwingAmount;
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", shift = At.Shift.AFTER))
    private void wings$animateModel(T living, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        WingsHooksClient.onSetPlayerRotationAngles(living, (PlayerModel<?>) (Object) this, ageInTicks, headPitch);
    }
}

