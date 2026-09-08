package cc.lvjia.wings.mixin;

import cc.lvjia.wings.server.asm.WingsHooks;
import cc.lvjia.wings.server.flight.FlightSpeedAntiCheat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修改服务器游戏包监听器，处理翅膀飞行时的移动检查
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "teleport(Lnet/minecraft/world/entity/PositionMoveRotation;Ljava/util/Set;)V", at = @At("TAIL"))
    private void wings$rebaseAntiCheatAfterTeleport(CallbackInfo ci) {
        FlightSpeedAntiCheat.onTeleport(player);
    }

    /**
     * 重定向飞行检查，允许翅膀飞行时的移动
     */
    @Redirect(method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isFallFlying()Z"))
    private boolean wings$redirectFlightCheck(ServerPlayer player) {
        return WingsHooks.onFlightCheck(player, player.isFallFlying());
    }
}
