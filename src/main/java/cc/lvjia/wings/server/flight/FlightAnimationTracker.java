package cc.lvjia.wings.server.flight;

import net.minecraft.world.entity.player.Player;

/**
 * 把飞行动画状态变化节流成可同步的节奏。
 * <p>
 * 状态切换会尽量快速同步；周期同步则用于补偿丢包和新追踪者加入后的收敛。
 */
public final class FlightAnimationTracker {
    private static final int DEFAULT_STATE_DELAY = 2;
    private static final int GLIDE_EXIT_LIFT_DELAY = 6;
    private static final int LAND_STATE_DELAY = 10;
    private static final int PERIODIC_SYNC_INTERVAL_TICKS = 23;
    private static final int TRANSITION_SYNC_COOLDOWN_TICKS = 4;

    private FlightAnimationState state = FlightAnimationState.IDLE;

    private int stateDelay = DEFAULT_STATE_DELAY;

    private int stateTime;

    private int syncCountdown = PERIODIC_SYNC_INTERVAL_TICKS;

    private int transitionSyncCooldown;

    private boolean pendingTransitionSync;

    public FlightAnimationState getState() {
        return this.state;
    }

    public void load(FlightAnimationState state) {
        this.state = state;
        this.stateDelay = this.resolveStateDelay(FlightAnimationState.IDLE, state);
        this.stateTime = 0;
        this.syncCountdown = PERIODIC_SYNC_INTERVAL_TICKS;
        this.transitionSyncCooldown = 0;
        this.pendingTransitionSync = false;
    }

    public boolean tick(Flight flight, Player player) {
        if (this.transitionSyncCooldown > 0) {
            this.transitionSyncCooldown--;
        }

        if (this.updateState(flight, player)) {
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

        if (this.state.shouldSyncPeriodically()) {
            if (--this.syncCountdown <= 0) {
                this.syncCountdown = PERIODIC_SYNC_INTERVAL_TICKS;
                return true;
            }
        } else {
            this.syncCountdown = PERIODIC_SYNC_INTERVAL_TICKS;
        }

        return false;
    }

    private boolean updateState(Flight flight, Player player) {
        if (this.stateTime++ <= this.stateDelay) {
            return false;
        }

        FlightAnimationState nextState = this.computeNextState(flight, player);
        if (nextState == this.state) {
            return false;
        }

        FlightAnimationState previousState = this.state;
        this.state = nextState;
        this.stateDelay = this.resolveStateDelay(previousState, nextState);
        this.stateTime = 0;
        return true;
    }

    private FlightAnimationState computeNextState(Flight flight, Player player) {
        double motionX = player.getX() - player.xo;
        double motionY = player.getY() - player.yo;
        double motionZ = player.getZ() - player.zo;

        if (flight.isFlying()) {
            if (motionY < 0.0D && player.getXRot() >= FlightAnimationRules.getPitch(motionX, motionY, motionZ)) {
                return FlightAnimationState.GLIDE;
            }
            return FlightAnimationState.LIFT;
        }

        if (motionY < 0.0D) {
            if (this.state == FlightAnimationState.IDLE && FlightAnimationRules.isNearGround(player)) {
                return FlightAnimationState.IDLE;
            }
            return flight.canLand(player) ? FlightAnimationState.LAND : FlightAnimationState.FALL;
        }

        return FlightAnimationState.IDLE;
    }

    private int resolveStateDelay(FlightAnimationState previousState, FlightAnimationState nextState) {
        if (nextState == FlightAnimationState.LAND) {
            return LAND_STATE_DELAY;
        }
        if (previousState == FlightAnimationState.GLIDE && nextState == FlightAnimationState.LIFT) {
            return GLIDE_EXIT_LIFT_DELAY;
        }
        return DEFAULT_STATE_DELAY;
    }

}
