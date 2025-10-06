package com.toni.wings.mixin;

import com.toni.wings.server.asm.WingsHooksClient;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;)V", at = @At("RETURN"))
    private void wings$animatePlayerModel(PlayerRenderState state, CallbackInfo ci) {
        WingsHooksClient.onSetPlayerRotationAngles(state, (PlayerModel) (Object) this);
    }
}
