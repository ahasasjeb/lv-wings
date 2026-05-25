package cc.lvjia.wings.server.asm;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

/**
 * 供 mixin/核心钩子调用的桥接方法集合。
 * <p>
 * 这些方法通常由注入点触发，负责创建并投递对应事件，或在必要时替换/增强原版逻辑。
 */
public final class WingsHooks {
    private WingsHooks() {
    }

    public static boolean onFlightCheck(LivingEntity living, boolean defaultValue) {
        return living instanceof Player && WingsHooks.onFlightCheck((Player) living, defaultValue);
    }

    public static boolean onFlightCheck(Player player, boolean defaultValue) {
        if (defaultValue) return true;
        PlayerFlightCheckEvent ev = new PlayerFlightCheckEvent(player);
        NeoForge.EVENT_BUS.post(ev);
        return ev.isFlying();
    }

    public static float onGetCameraEyeHeight(Entity entity, float eyeHeight) {
        GetCameraEyeHeightEvent ev = GetCameraEyeHeightEvent.create(entity, eyeHeight);
        NeoForge.EVENT_BUS.post(ev);
        return ev.getValue();
    }

    public static boolean onUpdateBodyRotation(LivingEntity living, float movementYaw) {
        GetLivingHeadLimitEvent ev = GetLivingHeadLimitEvent.create(living);
        NeoForge.EVENT_BUS.post(ev);
        if (ev.isVanilla()) return false;

        // 参考原版身体旋转逻辑，但允许通过事件动态调整软/硬限制。
        living.yBodyRot += Mth.wrapDegrees(movementYaw - living.yBodyRot) * 0.3F;
        float hLimit = ev.getHardLimit();
        float sLimit = ev.getSoftLimit();
        float theta = Mth.clamp(
                Mth.wrapDegrees(living.getYRot() - living.yBodyRot),
                -hLimit,
                hLimit
        );
        living.yBodyRot = living.getYRot() - theta;
        if (theta * theta > sLimit * sLimit) {
            living.yBodyRot += theta * 0.2F;
        }
        return true;
    }

    public static void onAddFlown(Player player, double x, double y, double z) {
        NeoForge.EVENT_BUS.post(new PlayerFlownEvent(player, new Vec3(x, y, z)));
    }
}
