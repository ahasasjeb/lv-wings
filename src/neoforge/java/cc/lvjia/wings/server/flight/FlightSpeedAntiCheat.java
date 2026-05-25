package cc.lvjia.wings.server.flight;

import cc.lvjia.wings.server.config.WingsConfig;
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

    private static final Map<UUID, TrackingState> STATES = new ConcurrentHashMap<>();

    private FlightSpeedAntiCheat() {
    }

    public static void tick(ServerPlayer player, Flight flight) {
        WingsConfig.FlightAntiCheatSettings settings = WingsConfig.getFlightAntiCheatSettings();
        if (!settings.enabled()) {
            clear(player);
            return;
        }

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
            if (flight.getTimeFlying() <= settings.takeoffGraceTicks() || player.onGround()) {
                state.captureSafePosition(player);
                state.relax();
            }
            return;
        }

        Vec3 safePosition = state.safePosition != null ? state.safePosition : player.position();
        state.pendingCorrection = null;
        state.cooldownUntilTick = player.tickCount + settings.correctionCooldownTicks();
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
        WingsConfig.FlightAntiCheatSettings settings = WingsConfig.getFlightAntiCheatSettings();
        if (!settings.enabled()) {
            clear(player);
            return;
        }

        if (!shouldMonitor(player, flight)) {
            clear(player);
            return;
        }

        TrackingState state = STATES.computeIfAbsent(player.getUUID(), key -> new TrackingState());
        if (player.tickCount < state.cooldownUntilTick) {
            return;
        }

        double horizontal = Math.hypot(movement.x(), movement.z());
        // 俯冲会天然放大负Y位移；这里只统计上升分量，避免把正常俯冲误判为超速。
        double vertical = Math.max(0.0D, movement.y());
        double total = Math.hypot(horizontal, vertical);
        double verticalBonus = computeUpwardVerticalBonus(horizontal, settings);
        double totalBonus = verticalBonus * 0.4D;
        double softVerticalLimit = settings.softVerticalLimit() + verticalBonus;
        double hardVerticalLimit = settings.hardVerticalLimit() + verticalBonus;
        double softTotalLimit = settings.softTotalLimit() + totalBonus;
        double hardTotalLimit = settings.hardTotalLimit() + totalBonus;

        if (flight.getTimeFlying() <= settings.takeoffGraceTicks() || player.isInWater() || player.isInLava()) {
            state.captureSafePosition(player);
            state.relax();
            return;
        }

        boolean hardViolation = horizontal > settings.hardHorizontalLimit()
            || vertical > hardVerticalLimit
            || total > hardTotalLimit;
        boolean softViolation = hardViolation
            || horizontal > settings.softHorizontalLimit()
            || vertical > softVerticalLimit
            || total > softTotalLimit;

        if (!softViolation) {
            state.captureSafePosition(player);
            state.relax();
            return;
        }

        state.softViolations++;
        state.hardViolations = hardViolation ? state.hardViolations + 1 : 0;
        if (state.softViolations >= settings.softViolationLimit() || state.hardViolations >= settings.hardViolationLimit()) {
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

    private static double computeUpwardVerticalBonus(double horizontal, WingsConfig.FlightAntiCheatSettings settings) {
        double threshold = settings.upwardAssistHorizontalThreshold();
        double maxBonus = settings.upwardAssistMaxBonus();
        if (threshold <= 0.0D || maxBonus <= 0.0D) {
            return 0.0D;
        }
        double ratio = 1.0D - Math.min(1.0D, horizontal / threshold);
        return maxBonus * ratio;
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
