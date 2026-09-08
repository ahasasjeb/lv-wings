package cc.lvjia.wings.server.flight;

import cc.lvjia.wings.server.config.FlightAntiCheatSettings;
import cc.lvjia.wings.server.config.WingsConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 翅膀飞行限速器。所有入口均在服务端主线程调用。 */
public final class FlightSpeedAntiCheat {
    private static final Logger LOGGER = LogManager.getLogger("WingsFlightAntiCheat");
    // 短暂收翅保留证据和起飞时间；正常停飞后由玩家 tick 回收，无需全表扫描。
    private static final Map<UUID, TrackingState> STATES = new HashMap<>();

    private FlightSpeedAntiCheat() {
    }

    public static void tick(ServerPlayer player, Flight flight) {
        FlightAntiCheatSettings settings = WingsConfig.getFlightAntiCheatSettings();
        if (!settings.enabled() || isExempt(player)) {
            clear(player);
            return;
        }
        TrackingState state = getState(player, flight.isFlying(), 0.0D, 0.0D, 0.0D);
        if (state == null) {
            return;
        }
        if (flight.isFlying()) {
            state.movement.markFlying(player.tickCount);
        }
        if (state.movement.isCoolingDown(player.tickCount)) {
            // 冷却是停飞期，不能成为客户端重新开翅后的免检窗口。
            flight.setIsFlying(false, Flight.PlayerSet.ofAll());
            state.movement.rebase();
            return;
        }
        if (state.impulseThisTick || player.isInPostImpulseGraceTime()) {
            // 仅信任服务端的外力宽限；不根据客户端 onGround、水或攀爬标志免检。
            state.impulseThisTick = false;
            state.movement.rebase();
            state.safePosition = player.position();
            return;
        }

        FlightMovementTracker.Result result = state.movement.finishTick(player.tickCount, System.nanoTime(), settings);
        if (result == FlightMovementTracker.Result.CORRECT) {
            Vec3 safe = state.safePosition;
            state.movement.onCorrection(player.tickCount, settings.correctionCooldownTicks());
            player.setDeltaMovement(Vec3.ZERO);
            player.fallDistance = 0.0F;
            flight.setIsFlying(false, Flight.PlayerSet.ofAll());
            player.connection.teleport(safe.x(), safe.y(), safe.z(), player.getYRot(), player.getXRot());
            LOGGER.info("Corrected sustained suspicious wings movement for {}", player.getPlainTextName());
        } else if (result == FlightMovementTracker.Result.CLEAN && state.movement.canAdvanceSafePosition()) {
            state.safePosition = player.position();
        }

        if (!flight.isFlying() && state.movement.canDiscard(player.tickCount)) {
            clear(player);
        }
    }

    /** 接收原版已接受的全部移动；不复用只统计空中饥饿消耗的事件。 */
    public static void recordMovement(ServerPlayer player, Flight flight, double x, double y, double z) {
        if (!WingsConfig.getFlightAntiCheatSettings().enabled() || isExempt(player)) {
            clear(player);
            return;
        }
        if (!flight.isFlying()) {
            return;
        }
        TrackingState state = getState(player, true, x, y, z);
        state.movement.markFlying(player.tickCount);
        state.impulseThisTick |= player.isInPostImpulseGraceTime();
        state.movement.recordMovement(x, y, z);
    }

    public static boolean canStartFlight(Player player) {
        if (!WingsConfig.getFlightAntiCheatSettings().enabled()) {
            return true;
        }
        TrackingState state = STATES.get(player.getUUID());
        return state == null || !state.movement.isCoolingDown(player.tickCount);
    }

    /** 同维度指令/末影珍珠/原版纠正传送后，旧坐标不再作为回退目标。 */
    public static void onTeleport(ServerPlayer player) {
        TrackingState state = STATES.get(player.getUUID());
        if (state != null) {
            state.safePosition = player.position();
            state.dimension = player.level().dimension();
            state.movement.rebase();
        }
    }

    public static void clear(Player player) {
        // 共享事件也可能在集成客户端执行，不能从客户端线程修改服务端状态表。
        if (player instanceof ServerPlayer) {
            STATES.remove(player.getUUID());
        }
    }

    private static TrackingState getState(ServerPlayer player, boolean create, double x, double y, double z) {
        UUID id = player.getUUID();
        TrackingState state = STATES.get(id);
        if (state != null && !state.dimension.equals(player.level().dimension())) {
            STATES.remove(id);
            state = null;
        }
        if (state == null && create) {
            // 首个包可能已经超速；初始安全点必须是这个包移动之前的位置。
            Vec3 beforeMovement = Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z)
                    ? player.position().subtract(x, y, z) : player.position();
            state = new TrackingState(player, beforeMovement);
            STATES.put(id, state);
        }
        return state;
    }

    private static boolean isExempt(ServerPlayer player) {
        return !player.isAlive() || player.isCreative() || player.isSpectator() || player.isPassenger()
                || player.isSleeping() || player.isChangingDimension();
    }

    private static final class TrackingState {
        private final FlightMovementTracker movement;
        private ResourceKey<Level> dimension;
        private Vec3 safePosition;
        private boolean impulseThisTick;

        private TrackingState(ServerPlayer player, Vec3 safePosition) {
            movement = new FlightMovementTracker(player.tickCount);
            dimension = player.level().dimension();
            this.safePosition = safePosition;
        }
    }
}
