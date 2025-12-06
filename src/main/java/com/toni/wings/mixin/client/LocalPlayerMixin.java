package com.toni.wings.mixin.client;

import com.toni.wings.server.asm.WingsHooks;
import net.minecraft.client.player.LocalPlayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
abstract class LocalPlayerMixin {
    @Shadow
    private boolean crouching;

    @Inject(method = "aiStep", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;crouching:Z", opcode = Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER))
    private void wings$preventCrouchWhileFlying(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        if (WingsHooks.onFlightCheck(self, self.isSwimming())) {
            this.crouching = false;
        }
    }
}
