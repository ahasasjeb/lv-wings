package cc.lvjia.wings.server.flight;

import cc.lvjia.wings.util.MathH;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

// 飞行动画判定规则：俯仰角计算 + 贴地检测
public final class FlightAnimationRules {
    private FlightAnimationRules() {
    }

    // 根据运动向量计算玩家的俯仰角，用于区分抬升(LIFT)和滑翔(GLIDE)
    public static float getPitch(double x, double y, double z) {
        return MathH.toDegrees((float) -Math.atan2(y, Mth.sqrt((float) (x * x + z * z))));
    }

    // 检测玩家脚下是否离地（向下 0.25 格检查方块）
    public static boolean isNearGround(Player player) {
        BlockPos below = BlockPos.containing(player.getX(), player.getY() - 0.25D, player.getZ());
        return !player.level().isEmptyBlock(below) || !player.level().isEmptyBlock(below.below());
    }
}
