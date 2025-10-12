package cc.lvjia.wings.mixin;

import cc.lvjia.wings.server.asm.WingsHooks;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修改生物实体行为，支持翅膀飞行
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    /**
     * 取消头部转动，用于翅膀飞行时的身体控制
     */
    @Inject(method = "tickHeadTurn(F)V", at = @At("HEAD"), cancellable = true)
    private void wings$cancelTickHeadTurn(float movementYaw, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (WingsHooks.onUpdateBodyRotation(self, movementYaw)) {
            ci.cancel();
        }
    }

    /**
     * 重定向视觉游泳检查，区分翅膀飞行和游泳状态
     */
    @Redirect(method = "isVisuallySwimming()Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isFallFlying()Z"))
    private boolean wings$redirectVisualSwimmingFlightCheck(LivingEntity instance) {
        return WingsHooks.onFlightCheck(instance, instance.isFallFlying());
    }
}
