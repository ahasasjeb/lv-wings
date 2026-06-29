package cc.lvjia.wings.mixin;

import cc.lvjia.wings.server.asm.WingsHooks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Mixin：挂钩空中移动统计，计算翅膀飞行或受控下降产生的饥饿消耗
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "checkMovementStatistics(DDD)V", at = @At("TAIL"))
    private void wings$trackFlight(double x, double y, double z, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        // 上游钩子位于原版移动统计最后的“空中、非鞘翅飞行”分支。这里保留 TAIL
        // 注入以降低对方法内部指令序号的耦合，但显式复刻该分支条件，避免把游泳、攀爬
        // 或地面移动误算成翅膀飞行/下降消耗。
        if (player.isPassenger()
                || x == 0.0D && y == 0.0D && z == 0.0D
                || player.isSwimming()
                || player.isEyeInFluid(FluidTags.WATER)
                || player.isInWater()
                || player.onClimbable()
                || player.onGround()
                || player.isFallFlying()) {
            return;
        }
        WingsHooks.onAddFlown(player, x, y, z);
    }
}
