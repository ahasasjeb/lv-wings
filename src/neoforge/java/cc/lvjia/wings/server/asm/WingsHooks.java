package cc.lvjia.wings.server.asm;

import cc.lvjia.wings.client.asm.GetCameraEyeHeightEvent;
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

        BodyRotationHookSupport.apply(living, movementYaw, ev.getHardLimit(), ev.getSoftLimit());
        return true;
    }

    public static void onAddFlown(Player player, double x, double y, double z) {
        NeoForge.EVENT_BUS.post(new PlayerFlownEvent(player, new Vec3(x, y, z)));
    }
}
