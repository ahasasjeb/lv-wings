package com.toni.wings.mixin.client;

import com.toni.wings.server.asm.WingsHooks;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Redirect(method = "tick()V",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getEyeHeight()F"))
    private float wings$applyCameraEyeHeight(Entity entity) {
        float vanilla = entity.getEyeHeight();
        return WingsHooks.onGetCameraEyeHeight(entity, vanilla);
    }
}
