package cc.lvjia.wings.mixin;

import cc.lvjia.wings.client.hooks.WingsHooksClient;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修改相机视角高度，用于翅膀飞行时的视角调整
 */
@Mixin(Camera.class)
public abstract class FabricCameraMixin {
    @Shadow
    @Final
    private Quaternionf rotation;

    @Shadow
    @Final
    private Vector3f forwards;

    @Shadow
    @Final
    private Vector3f up;

    @Shadow
    @Final
    private Vector3f left;

    @Shadow
    private float xRot;

    @Shadow
    private float yRot;

    @Shadow
    private int matrixPropertiesDirty;

    /**
     * 重定向实体眼睛高度获取，用于飞行时的视角调整
     */
    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getEyeHeight()F"))
    private float wings$modifyEyeHeight(Entity entity) {
        return WingsHooksClient.onGetCameraEyeHeight(entity, entity.getEyeHeight());
    }

    @Inject(method = "alignWithEntity(F)V", at = @At("TAIL"))
    private void wings$applyCameraRoll(float partialTicks, CallbackInfo ci) {
        float roll = WingsHooksClient.onComputeCameraRoll(partialTicks);
        if (roll != 0.0F) {
            this.rotation.rotationYXZ(3.1415927F - this.yRot * 0.017453292F, -this.xRot * 0.017453292F,
                    -roll * 0.017453292F);
            this.forwards.set(0.0F, 0.0F, -1.0F).rotate(this.rotation);
            this.up.set(0.0F, 1.0F, 0.0F).rotate(this.rotation);
            this.left.set(-1.0F, 0.0F, 0.0F).rotate(this.rotation);
            this.matrixPropertiesDirty |= 3;
        }
    }
}
