package cc.lvjia.wings.mixin;

import cc.lvjia.wings.server.asm.WingsHooks;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Mixin：翅膀飞行时复用鞘翅飞行姿态（FALL_FLYING），使模型渲染为飞行姿势
@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "getDesiredPose()Lnet/minecraft/world/entity/Pose;", at = @At("RETURN"), cancellable = true)
    private void wings$useFallFlyingPoseForWingFlight(CallbackInfoReturnable<Pose> cir) {
        Player player = (Player) (Object) this;
        Pose pose = cir.getReturnValue();
        // 不覆盖睡眠和游泳状态，其余正常飞行时替换为 FALL_FLYING 姿势
        if (pose != Pose.SLEEPING && pose != Pose.SWIMMING && WingsHooks.onFlightCheck(player, player.isFallFlying())) {
            cir.setReturnValue(Pose.FALL_FLYING);
        }
    }
}
