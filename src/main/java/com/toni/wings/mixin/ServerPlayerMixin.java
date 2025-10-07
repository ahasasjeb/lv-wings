package com.toni.wings.mixin;

import com.toni.wings.server.asm.WingsHooks;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "checkMovementStatistics(DDD)V", at = @At("TAIL"))
    private void wings$trackFlightDistance(double dx, double dy, double dz, CallbackInfo ci) {
        WingsHooks.onAddFlown((ServerPlayer) (Object) this, dx, dy, dz);
    }
}
