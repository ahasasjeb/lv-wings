package cc.lvjia.wings.mixin;

import cc.lvjia.wings.server.asm.WingsHooks;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
        return WingsHooks.onGetCameraEyeHeight(entity, entity.getEyeHeight());
    }
}
