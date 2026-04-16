package cc.lvjia.wings.server.flight;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 保守的翅膀飞行限速器。
 * <p>
 * 这里只处理明显异常的持续超速，避免把瞬时外力或起飞阶段误判成作弊。
 */
public final class FlightSpeedAntiCheat {
    private static final Logger LOGGER = LogManager.getLogger("WingsFlightAntiCheat");

    private static final int TAKEOFF_GRACE_TICKS = 8;
    private static final int SOFT_VIOLATION_LIMIT = 4;
    private static final int HARD_VIOLATION_LIMIT = 2;
    private static final int CORRECTION_COOLDOWN_TICKS = 10;

    private static final double SOFT_HORIZONTAL_LIMIT = 1.6D;
    private static final double SOFT_VERTICAL_LIMIT = 1.2D;
    private static final double SOFT_TOTAL_LIMIT = 1.85D;

    private static final double HARD_HORIZONTAL_LIMIT = 2.4D;
    private static final double HARD_VERTICAL_LIMIT = 1.8D;
    private static final double HARD_TOTAL_LIMIT = 2.75D;

    private static final Map<UUID, TrackingState> STATES = new ConcurrentHashMap<>();

    private FlightSpeedAntiCheat() {
    }

    public static void tick(ServerPlayer player, Flight flight) {
        TrackingState state = STATES.get(player.getUUID());
        if (state == null && !shouldMonitor(player, flight)) {
            return;
        }

        if (state == null) {
            state = STATES.computeIfAbsent(player.getUUID(), key -> new TrackingState());
        }

        if (player.tickCount < state.cooldownUntilTick) {
            return;
        }

        PendingCorrection correction = state.pendingCorrection;
        if (correction == null && !shouldMonitor(player, flight)) {
            clear(player);
            return;
        }

        if (correction == null) {
            if (flight.getTimeFlying() <= TAKEOFF_GRACE_TICKS || player.onGround()) {
                state.captureSafePosition(player);
                state.relax();
            }
            return;
        }

        Vec3 safePosition = state.safePosition != null ? state.safePosition : player.position();
        state.pendingCorrection = null;
        state.cooldownUntilTick = player.tickCount + CORRECTION_COOLDOWN_TICKS;
        state.relax();
        state.safePosition = safePosition;

        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        flight.setIsFlying(false, Flight.PlayerSet.ofAll());
        player.connection.teleport(safePosition.x(), safePosition.y(), safePosition.z(), player.getYRot(), player.getXRot());

        LOGGER.info(
                "Corrected suspicious wings flight speed for {} (horizontal={}, vertical={}, total={})",
                player.getPlainTextName(),
                String.format("%.3f", correction.horizontal()),
                String.format("%.3f", correction.vertical()),
                String.format("%.3f", correction.total())
        );
    }

    public static void recordMovement(ServerPlayer player, Flight flight, Vec3 movement) {
        if (!shouldMonitor(player, flight)) {
            clear(player);
            return;
        }

        TrackingState state = STATES.computeIfAbsent(player.getUUID(), key -> new TrackingState());
        if (player.tickCount < state.cooldownUntilTick) {
            return;
        }

        double horizontal = Math.hypot(movement.x(), movement.z());
        double vertical = Math.abs(movement.y());
        double total = movement.length();

        if (flight.getTimeFlying() <= TAKEOFF_GRACE_TICKS || player.isInWater() || player.isInLava()) {
            state.captureSafePosition(player);
            state.relax();
            return;
        }

        boolean hardViolation = horizontal > HARD_HORIZONTAL_LIMIT || vertical > HARD_VERTICAL_LIMIT || total > HARD_TOTAL_LIMIT;
        boolean softViolation = hardViolation
                || horizontal > SOFT_HORIZONTAL_LIMIT
                || vertical > SOFT_VERTICAL_LIMIT
                || total > SOFT_TOTAL_LIMIT;

        if (!softViolation) {
            state.captureSafePosition(player);
            state.relax();
            return;
        }

        state.softViolations++;
        state.hardViolations = hardViolation ? state.hardViolations + 1 : 0;
        if (state.softViolations >= SOFT_VIOLATION_LIMIT || state.hardViolations >= HARD_VIOLATION_LIMIT) {
            state.pendingCorrection = new PendingCorrection(horizontal, vertical, total);
        }
    }

    public static void clear(Player player) {
        STATES.remove(player.getUUID());
    }

    private static boolean shouldMonitor(ServerPlayer player, Flight flight) {
        return flight.isFlying()
                && !player.isCreative()
                && !player.isSpectator()
                && !player.isPassenger()
                && !player.isSleeping()
                && !player.isChangingDimension();
    }

    private record PendingCorrection(double horizontal, double vertical, double total) {
    }

    private static final class TrackingState {
        private Vec3 safePosition;
        private int softViolations;
        private int hardViolations;
        private int cooldownUntilTick;
        private PendingCorrection pendingCorrection;

        private void captureSafePosition(ServerPlayer player) {
            this.safePosition = player.position();
        }

        private void relax() {
            this.softViolations = Math.max(0, this.softViolations - 1);
            this.hardViolations = 0;
        }
    }
}
