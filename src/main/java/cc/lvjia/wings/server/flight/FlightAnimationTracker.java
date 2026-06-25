package cc.lvjia.wings.server.flight;

import net.minecraft.world.entity.player.Player;

/**
 * 把飞行动画状态变化节流成可同步的节奏。
 * <p>
 * 状态切换会尽量快速同步；周期同步则用于补偿丢包和新追踪者加入后的收敛。
 */
public final class FlightAnimationTracker {
    private static final int PERIODIC_SYNC_INTERVAL_TICKS = 23;
    private static final int TRANSITION_SYNC_COOLDOWN_TICKS = 4;

    private final FlightAnimationEngine engine = new FlightAnimationEngine();

    private int syncCountdown = PERIODIC_SYNC_INTERVAL_TICKS;

    private int transitionSyncCooldown;

    private boolean pendingTransitionSync;

    public FlightAnimationState getState() {
        return this.engine.getState();
    }

    public void load(FlightAnimationState state) {
        this.engine.load(state);
        this.syncCountdown = PERIODIC_SYNC_INTERVAL_TICKS;
        this.transitionSyncCooldown = 0;
        this.pendingTransitionSync = false;
    }

    public boolean tick(Flight flight, Player player) {
        if (this.transitionSyncCooldown > 0) {
            this.transitionSyncCooldown--;
        }

        if (this.engine.tick(flight, player)) {
            this.syncCountdown = PERIODIC_SYNC_INTERVAL_TICKS;
            if (this.transitionSyncCooldown == 0) {
                this.transitionSyncCooldown = TRANSITION_SYNC_COOLDOWN_TICKS;
                this.pendingTransitionSync = false;
                return true;
            }
            this.pendingTransitionSync = true;
        }

        if (this.pendingTransitionSync && this.transitionSyncCooldown == 0) {
            this.transitionSyncCooldown = TRANSITION_SYNC_COOLDOWN_TICKS;
            this.pendingTransitionSync = false;
            this.syncCountdown = PERIODIC_SYNC_INTERVAL_TICKS;
            return true;
        }

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