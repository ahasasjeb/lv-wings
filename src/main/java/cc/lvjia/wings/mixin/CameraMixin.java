package cc.lvjia.wings.mixin;

import cc.lvjia.wings.client.hooks.WingsHooksClient;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修改相机视角高度，用于翅膀飞行时的视角调整
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    /**
     * 重定向实体眼睛高度获取，用于飞行时的视角调整
     */
    @Redirect(method = "tick()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getEyeHeight()F"))
    private float wings$modifyEyeHeight(Entity entity) {
        return WingsHooksClient.onGetCameraEyeHeight(entity, entity.getEyeHeight());
    }

    /**
     * Fabric 没有 NeoForge 的 ComputeCameraAngles 事件，这里在提取相机渲染状态时应用同样的 roll。
     */
    @Inject(method = "extractRenderState(Lnet/minecraft/client/renderer/state/level/CameraRenderState;F)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;getViewRotationMatrix(Lorg/joml/Matrix4f;)Lorg/joml/Matrix4f;",
                    shift = At.Shift.AFTER))
    private void wings$applyCameraRoll(CameraRenderState cameraState, float cameraEntityPartialTicks, CallbackInfo ci) {
        float roll = WingsHooksClient.onComputeCameraRoll(cameraEntityPartialTicks);
        if (roll != 0.0F) {
            cameraState.viewRotationMatrix.rotateZ(-roll * 0.017453292F);
        }
    }
}
