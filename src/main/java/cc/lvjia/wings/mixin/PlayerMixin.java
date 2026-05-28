package cc.lvjia.wings.mixin;

import cc.lvjia.wings.server.asm.WingsHooks;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "getDesiredPose()Lnet/minecraft/world/entity/Pose;", at = @At("RETURN"), cancellable = true)
    private void wings$useFallFlyingPoseForWingFlight(CallbackInfoReturnable<Pose> cir) {
        Player player = (Player) (Object) this;
        Pose pose = cir.getReturnValue();
        if (pose != Pose.SLEEPING && pose != Pose.SWIMMING && WingsHooks.onFlightCheck(player, player.isFallFlying())) {
            cir.setReturnValue(Pose.FALL_FLYING);
        }
    }
}
