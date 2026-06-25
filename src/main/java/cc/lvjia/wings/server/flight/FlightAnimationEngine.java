package cc.lvjia.wings.server.flight;

import net.minecraft.world.entity.player.Player;

/**
 * 飞行动画状态机核心逻辑，客户端预测与服务端权威计算共用。
 */
public final class FlightAnimationEngine {
    public static final int DEFAULT_STATE_DELAY = 2;

    public static final int GLIDE_EXIT_LIFT_DELAY = 6;

    public static final int LAND_STATE_DELAY = 10;

    private FlightAnimationState state = FlightAnimationState.IDLE;

    private int stateDelay = DEFAULT_STATE_DELAY;

    private int stateTime;

    public FlightAnimationState getState() {
        return this.state;
    }

    public void load(FlightAnimationState state) {
        this.state = state;
        this.stateDelay = resolveStateDelay(FlightAnimationState.IDLE, state);
        this.stateTime = 0;
    }

    public boolean tick(Flight flight, Player player) {
        if (this.stateTime++ <= this.stateDelay) {
            return false;
        }

        FlightAnimationState nextState = computeNextState(this.state, flight, player);
        if (nextState == this.state) {
            return false;
        }

        FlightAnimationState previousState = this.state;
        this.state = nextState;
        this.stateDelay = resolveStateDelay(previousState, nextState);
        this.stateTime = 0;
        return true;
    }

    public static FlightAnimationState computeNextState(FlightAnimationState currentState, Flight flight, Player player) {
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
            if (currentState == FlightAnimationState.IDLE && FlightAnimationRules.isNearGround(player)) {
                return FlightAnimationState.IDLE;
            }
            return flight.canLand(player) ? FlightAnimationState.LAND : FlightAnimationState.FALL;
        }

        return FlightAnimationState.IDLE;
    }

    public static int resolveStateDelay(FlightAnimationState previousState, FlightAnimationState nextState) {
        if (nextState == FlightAnimationState.LAND) {
            return LAND_STATE_DELAY;
        }
        if (previousState == FlightAnimationState.GLIDE && nextState == FlightAnimationState.LIFT) {
            return GLIDE_EXIT_LIFT_DELAY;
        }
        return DEFAULT_STATE_DELAY;
    }
}