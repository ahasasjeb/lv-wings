package com.toni.wings.mixin;

import com.toni.wings.server.asm.WingsHooks;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Redirect(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isFallFlying()Z", ordinal = 0))
    private boolean wings$wrapFlightCheckZero(ServerPlayer instance) {
        return WingsHooks.onFlightCheck(instance, instance.isFallFlying());
    }

    @Redirect(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isFallFlying()Z", ordinal = 1))
    private boolean wings$wrapFlightCheckOne(ServerPlayer instance) {
        return WingsHooks.onFlightCheck(instance, instance.isFallFlying());
    }

    @Redirect(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isFallFlying()Z", ordinal = 2))
    private boolean wings$wrapFlightCheckTwo(ServerPlayer instance) {
        return WingsHooks.onFlightCheck(instance, instance.isFallFlying());
    }

    @Inject(method = "handlePlayerAbilities", at = @At("TAIL"))
    private void wings$onHandlePlayerAbilities(ServerboundPlayerAbilitiesPacket packet, CallbackInfo ci) {
        WingsHooks.onPlayerAbilities(this.player, packet.isFlying());
    }
}
