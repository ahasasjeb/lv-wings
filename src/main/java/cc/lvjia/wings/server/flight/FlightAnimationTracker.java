package cc.lvjia.wings.server.flight;

import net.minecraft.world.entity.player.Player;

/**
 * 把飞行动画状态变化节流成可同步的节奏。
 * <p>
 * 状态切换会尽量快速同步；周期同步则用于补偿丢包和新追踪者加入后的收敛。
 */
public final class FlightAnimationTracker {
    // 周期同步间隔：~23 tick ≈ 1.15 秒发一次包
    private static final int PERIODIC_SYNC_INTERVAL_TICKS = 23;
    // 状态切换后的冷却：避免高频切换时连续发包
    private static final int TRANSITION_SYNC_COOLDOWN_TICKS = 4;

    private final FlightAnimationEngine engine = new FlightAnimationEngine();

    private int syncCountdown = PERIODIC_SYNC_INTERVAL_TICKS;

    private int transitionSyncCooldown;

    private boolean pendingTransitionSync;

    public FlightAnimationState getState() {
        return this.engine.getState();
    }

    // 强制加载状态后重置定时器，避免刚同步就发冗余包
    public void load(FlightAnimationState state) {
        this.engine.load(state);
        this.syncCountdown = PERIODIC_SYNC_INTERVAL_TICKS;
        this.transitionSyncCooldown = 0;
        this.pendingTransitionSync = false;
    }

    // 每 tick 推进状态机，返回 true 表示需要向其他客户端同步
    public boolean tick(Flight flight, Player player) {
        if (this.transitionSyncCooldown > 0) {
            this.transitionSyncCooldown--;
        }

        // 状态机发生了切换
        if (this.engine.tick(flight, player)) {
            this.syncCountdown = PERIODIC_SYNC_INTERVAL_TICKS;
            if (this.transitionSyncCooldown == 0) {
                // 冷却已结束，立即同步
                this.transitionSyncCooldown = TRANSITION_SYNC_COOLDOWN_TICKS;
                this.pendingTransitionSync = false;
                return true;
            }
            // 冷却中，标记待同步
            this.pendingTransitionSync = true;
        }

        // 处理积压的待同步状态切换
        if (this.pendingTransitionSync && this.transitionSyncCooldown == 0) {
            this.transitionSyncCooldown = TRANSITION_SYNC_COOLDOWN_TICKS;
            this.pendingTransitionSync = false;
            this.syncCountdown = PERIODIC_SYNC_INTERVAL_TICKS;
            return true;
        }

        // 飞行中状态定期同步（补偿丢包和新追踪者）
        if (this.engine.getState().shouldSyncPeriodically()) {
            if (--this.syncCountdown <= 0) {
                this.syncCountdown = PERIODIC_SYNC_INTERVAL_TICKS;
                return true;
            }
        } else {
            this.syncCountdown = PERIODIC_SYNC_INTERVAL_TICKS;
        }

        return false;
    }

}