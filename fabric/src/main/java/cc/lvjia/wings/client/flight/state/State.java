package cc.lvjia.wings.client.flight.state;

import cc.lvjia.wings.client.flight.Animator;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.util.MathH;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

/**
 * 客户端飞行动画状态机。
 * <p>
 * 根据玩家速度向量与飞行/下落情况在若干状态间切换，并驱动 {@link Animator} 播放对应动画。
 */
public abstract class State {
    static final int STATE_DELAY = 2;

    private final int stateDelay;

    private final Consumer<Animator> animation;

    private int time;

    protected State(Consumer<Animator> animation) {
        this(STATE_DELAY, animation);
    }

    protected State(int stateDelay, Consumer<Animator> animation) {
        this.stateDelay = stateDelay;
        this.animation = animation;
    }

    public final State update(Flight flight, double x, double y, double z, Player player) {
        // 增加一点延迟，避免速度抖动导致状态频繁切换。
        if (this.time++ > this.stateDelay) {
            return this.getNext(flight, x, y, z, player);
        }
        return this;
    }

    private State getNext(Flight flight, double x, double y, double z, Player player) {
        if (flight.isFlying()) {
            if (y < 0 && player.getXRot() >= this.getPitch(x, y, z)) {
                return this.createGlide();
            }
            return this.createLift();
        }
        if (y < 0) {
            return this.getDescent(flight, player);
        }
        return this.getDefault(y);
    }

    private float getPitch(double x, double y, double z) {
        // 根据速度向量计算“俯仰角阈值”，用于判断何时进入滑翔。
        return MathH.toDegrees((float) -Math.atan2(y, Mth.sqrt((float) (x * x + z * z))));
    }

    public final void beginAnimation(Animator animator) {
        this.animation.accept(animator);
    }

    protected State createLand() {
        return new StateLand();
    }

    protected State createLift() {
        return new StateLift();
    }

    protected State createGlide() {
        return new StateGlide();
    }

    protected State createIdle() {
        return new StateIdle();
    }

    protected State createFall() {
        return new StateFall();
    }

    protected State getDefault(double y) {
        return this.createIdle();
    }

    protected State getDescent(Flight flight, Player player) {
        return flight.canLand(player) ? this.createLand() : this.createFall();
    }
}
