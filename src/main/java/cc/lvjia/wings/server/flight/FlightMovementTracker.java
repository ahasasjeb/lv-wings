package cc.lvjia.wings.server.flight;

import cc.lvjia.wings.server.config.FlightAntiCheatSettings;

/**
 * 纯数值的服务端移动记账器。包只累计路程；预算、违规和恢复每 tick 最多结算一次。
 * 使用固定大小的预算，不保存包历史，也不随延迟无限扩大容差。
 */
final class FlightMovementTracker {
    private static final int BURST_TICKS = 3;
    private static final int CLEAN_TICKS_TO_RELAX = 20;
    private static final double EPSILON = 1.0E-7D;
    private static final double NANOS_PER_TICK = 50_000_000.0D;

    private final MovementBudget softBudget = new MovementBudget();
    private final MovementBudget hardBudget = new MovementBudget();
    private final int startedAtTick;
    private int lastCheckedTick;
    private long lastCheckNanos;
    private boolean clockStarted;
    private int lastFlyingTick;
    private int cooldownStartedTick;
    private int cooldownTicks;
    private int softViolations;
    private int hardViolations;
    private int cleanTicks;
    private boolean safePositionFrozen;
    private double horizontal;
    private double upward;
    private double downward;
    private boolean invalidMovement;

    FlightMovementTracker(int tick) {
        startedAtTick = tick;
        lastCheckedTick = tick - 1;
        lastFlyingTick = tick;
    }

    void recordMovement(double x, double y, double z) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            invalidMovement = true;
            return;
        }
        // 路程而非净位移：往返或上升/下降不能互相抵消，拆包也不会新增额度。
        horizontal += Math.hypot(x, z);
        upward += Math.max(0.0D, y);
        downward += Math.max(0.0D, -y);
    }

    Result finishTick(int tick, long nowNanos, FlightAntiCheatSettings settings) {
        // int 差值也支持正常的 tickCount 溢出；实体重建由生命周期钩子清理。
        int elapsed = tick - lastCheckedTick;
        if (elapsed <= 0) {
            return Result.UNCHANGED;
        }
        lastCheckedTick = tick;
        // 服务端持续低 TPS 时，客户端仍按实际时间移动。只补偿已流逝的时间，且单次最多 3 tick。
        double elapsedBudget = Math.min(BURST_TICKS, Math.max(elapsed,
                clockStarted ? (nowNanos - lastCheckNanos) / NANOS_PER_TICK : 1.0D));
        lastCheckNanos = nowNanos;
        clockStarted = true;
        double bonus = FlightMovementChecks.computeUpwardVerticalBonus(horizontal / elapsedBudget, settings);
        double total = Math.hypot(horizontal, upward);
        boolean hard = hardBudget.exceeds(elapsedBudget, horizontal, upward, downward, total,
                settings.hardHorizontalLimit(), settings.hardVerticalLimit() + bonus,
                settings.hardDownwardLimit(), settings.hardTotalLimit() + bonus * 0.4D);
        boolean soft = softBudget.exceeds(elapsedBudget, horizontal, upward, downward, total,
                settings.softHorizontalLimit(), settings.softVerticalLimit() + bonus,
                settings.softDownwardLimit(), settings.softTotalLimit() + bonus * 0.4D);
        hard |= invalidMovement;
        // 宽限采用独立的服务端时间，不能被动画上限或短暂收翅重置。硬阈值始终有效。
        soft = hard || soft && tick - startedAtTick >= settings.takeoffGraceTicks();
        clearMovement();

        if (soft) {
            cleanTicks = 0;
            safePositionFrozen = true;
            softViolations = Math.min(50, softViolations + 1);
            if (hard) {
                hardViolations = Math.min(50, hardViolations + 1);
            }
            return softViolations >= settings.softViolationLimit()
                    || hardViolations >= settings.hardViolationLimit() ? Result.CORRECT : Result.SUSPECT;
        }

        if (++cleanTicks >= CLEAN_TICKS_TO_RELAX) {
            cleanTicks = 0;
            softViolations = Math.max(0, softViolations - 1);
            hardViolations = Math.max(0, hardViolations - 1);
            safePositionFrozen = false;
        }
        return Result.CLEAN;
    }

    boolean canAdvanceSafePosition() {
        return !safePositionFrozen;
    }

    /** 服务端传送/外力使当前采样失效，但不刷新起飞宽限或删除此前的违规。 */
    void rebase() {
        clearMovement();
        softBudget.reset();
        hardBudget.reset();
        cleanTicks = 0;
    }

    void onCorrection(int tick, int cooldown) {
        rebase();
        softViolations = 0;
        hardViolations = 0;
        safePositionFrozen = false;
        cooldownStartedTick = tick;
        cooldownTicks = cooldown;
    }

    void markFlying(int tick) {
        lastFlyingTick = tick;
    }

    boolean isCoolingDown(int tick) {
        return cooldownTicks > 0 && tick - cooldownStartedTick < cooldownTicks;
    }

    boolean canDiscard(int tick) {
        // 短暂收翅保留证据，正常停飞两秒后回收；更长的配置冷却优先。
        return tick - lastFlyingTick >= 40 && !isCoolingDown(tick);
    }

    private void clearMovement() {
        horizontal = 0.0D;
        upward = 0.0D;
        downward = 0.0D;
        invalidMovement = false;
    }

    enum Result {
        UNCHANGED, CLEAN, SUSPECT, CORRECT
    }

    /** 每个方向以“允许移动的 tick 数”计费，空闲时最多保留 3 tick 额度。 */
    private static final class MovementBudget {
        private final double[] credit = new double[4];

        private MovementBudget() {
            reset();
        }

        private void reset() {
            java.util.Arrays.fill(credit, BURST_TICKS);
        }

        private boolean exceeds(double elapsed, double horizontal, double upward, double downward, double total,
                                double horizontalLimit, double upwardLimit, double downwardLimit, double totalLimit) {
            // 必须结算全部分量，不能用短路或跳过其他方向的扣款。
            boolean exceeded = consume(0, elapsed, horizontal, horizontalLimit);
            exceeded |= consume(1, elapsed, upward, upwardLimit);
            exceeded |= consume(2, elapsed, downward, downwardLimit);
            exceeded |= consume(3, elapsed, total, totalLimit);
            return exceeded;
        }

        private boolean consume(int axis, double elapsed, double distance, double limit) {
            double cost = distance == 0.0D ? 0.0D : limit > 0.0D ? distance / limit : Double.POSITIVE_INFINITY;
            double remaining = Math.min(BURST_TICKS, credit[axis] + elapsed) - cost;
            // 单次尖峰只记一次违规，不把巨额欠款转嫁给之后的正常移动。
            credit[axis] = Math.max(0.0D, remaining);
            return remaining < -EPSILON;
        }
    }
}
