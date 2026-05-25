package cc.lvjia.wings.server.flight;

import cc.lvjia.wings.util.MathH;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public final class FlightAnimationRules {
    private FlightAnimationRules() {
    }

    public static float getPitch(double x, double y, double z) {
        return MathH.toDegrees((float) -Math.atan2(y, Mth.sqrt((float) (x * x + z * z))));
    }

    public static boolean isNearGround(Player player) {
        BlockPos below = BlockPos.containing(player.getX(), player.getY() - 0.25D, player.getZ());
        return !player.level().isEmptyBlock(below) || !player.level().isEmptyBlock(below.below());
    }
}
