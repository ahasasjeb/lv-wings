package com.toni.wings.mixin.client;

import com.toni.wings.server.asm.WingsHooks;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Camera.class)
abstract class CameraMixin {

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getEyeHeight()F"))
    private float wings$wrapEyeHeight(Entity instance) {
        float eyeHeight = instance.getEyeHeight();
        return WingsHooks.onGetCameraEyeHeight(instance, eyeHeight);
    }
}
