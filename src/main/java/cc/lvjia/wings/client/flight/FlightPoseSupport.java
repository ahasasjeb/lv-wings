package cc.lvjia.wings.client.flight;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.WeakHashMap;

public final class FlightPoseSupport {
    private static final Map<Player, FlightPoseAngles> FLIGHT_POSE_ANGLES = new WeakHashMap<>();

    private FlightPoseSupport() {
    }

    public static boolean shouldApplyFlightPose(Player player, float amount) {
        return amount > 0.0F && !player.isSpectator();
    }

    public static FlightPoseAngles getFlightPoseAngles(Player player, boolean flying, float amount, float delta) {
        FlightPoseAngles angles = FLIGHT_POSE_ANGLES.computeIfAbsent(player, ignored -> new FlightPoseAngles());
        if (flying) {
            angles.roll = getBodyYawRoll(player, delta);
            angles.pitch = -Mth.lerp(delta, player.xRotO, player.getXRot()) - 90.0F;
        } else if (amount <= 0.0F) {
            FLIGHT_POSE_ANGLES.remove(player);
        }
        return angles;
    }

    public static void clear(Player player) {
        FLIGHT_POSE_ANGLES.remove(player);
    }

    public static void clearAll() {
        FLIGHT_POSE_ANGLES.clear();
    }

    private static float getBodyYawRoll(Player player, float delta) {
        float diffO = Mth.wrapDegrees(player.yBodyRotO - player.yRotO);
        float diff = Mth.wrapDegrees(player.yBodyRot - player.getYRot());
        return Mth.rotLerp(delta, diffO, diff);
    }

    public static final class FlightPoseAngles {
        private float roll;
        private float pitch;

        public float roll() {
            return this.roll;
        }

        public float pitch() {
            return this.pitch;
        }
    }
}
