package cc.lvjia.wings.server.flight;

import net.minecraft.world.entity.player.Player;

/**
 * 飞行动画状态机核心逻辑，客户端预测与服务端权威计算共用。
 */
public final class FlightAnimationEngine {
    // 各状态转换的等待 tick 数，用于平滑过渡
    public static final int DEFAULT_STATE_DELAY = 2;

    // 滑翔→抬升：稍长延迟，避免视角抖动
    public static final int GLIDE_EXIT_LIFT_DELAY = 6;

    // 着陆动画保持时长，让翅膀收拢动作完整播放
    public static final int LAND_STATE_DELAY = 10;

    private FlightAnimationState state = FlightAnimationState.IDLE;

    private int stateDelay = DEFAULT_STATE_DELAY;

    private int stateTime;

    public FlightAnimationState getState() {
        return this.state;
    }

    // 强制加载指定状态（网络同步或克隆时使用）
    public void load(FlightAnimationState state) {
        this.state = state;
        this.stateDelay = resolveStateDelay(FlightAnimationState.IDLE, state);
        this.stateTime = 0;
    }

    // 每 tick 推进状态机，返回 true 表示状态发生切换
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

    // 根据运动方向和玩家俯仰计算下一状态（无副作用，纯函数）
    public static FlightAnimationState computeNextState(FlightAnimationState currentState, Flight flight, Player player) {
        double motionX = player.getX() - player.xo;
        double motionY = player.getY() - player.yo;
        double motionZ = player.getZ() - player.zo;

        if (flight.isFlying()) {
            // 下降且俯仰够陡 → 滑翔；否则正常抬升
            if (motionY < 0.0D && player.getXRot() >= FlightAnimationRules.getPitch(motionX, motionY, motionZ)) {
                return FlightAnimationState.GLIDE;
            }
            return FlightAnimationState.LIFT;
        }

        if (motionY < 0.0D) {
            // 空闲时贴地不做着陆动画，避免频繁切换
            if (currentState == FlightAnimationState.IDLE && FlightAnimationRules.isNearGround(player)) {
                return FlightAnimationState.IDLE;
            }
            return flight.canLand(player) ? FlightAnimationState.LAND : FlightAnimationState.FALL;
        }

        return FlightAnimationState.IDLE;
    }

    // 根据状态转换对返回延迟 tick 数
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