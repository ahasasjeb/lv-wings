package cc.lvjia.wings.server.asm;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public final class BodyRotationHookSupport {
    private BodyRotationHookSupport() {
    }

    public static void apply(LivingEntity living, float movementYaw, float hardLimit, float softLimit) {
        living.yBodyRot += Mth.wrapDegrees(movementYaw - living.yBodyRot) * 0.3F;
        float theta = Mth.clamp(
                Mth.wrapDegrees(living.getYRot() - living.yBodyRot),
                -hardLimit,
                hardLimit);
        living.yBodyRot = living.getYRot() - theta;
        if (theta * theta > softLimit * softLimit) {
            living.yBodyRot += theta * 0.2F;
        }
    }
}
