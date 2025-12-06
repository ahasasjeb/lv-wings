package com.toni.wings.mixin.client;

import com.toni.wings.server.asm.WingsHooksClient;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
abstract class EntityTurnMixin {
    @Inject(method = "turn(DD)V", at = @At("TAIL"))
    private void wings$notifyTurn(double yawChange, double pitchChange, CallbackInfo ci) {
        float yawDelta = (float) (yawChange * 0.15F);
        WingsHooksClient.onTurn((Entity) (Object) this, yawDelta);
    }
}
