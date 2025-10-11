package cc.lvjia.wings.mixin;

import cc.lvjia.wings.server.asm.WingsHooks;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Redirect(method = "tick()V",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getEyeHeight()F"))
    private float wings$modifyEyeHeight(Entity entity) {
        return WingsHooks.onGetCameraEyeHeight(entity, entity.getEyeHeight());
    }
}
