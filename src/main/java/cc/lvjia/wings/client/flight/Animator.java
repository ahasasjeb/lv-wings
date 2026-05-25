package cc.lvjia.wings.client.flight;

/**
 * 客户端翅膀/飞行相关动画驱动接口。
 * <p>
 * 状态机在切换状态时调用 begin* 方法触发过渡，并在每 tick 调用 {@link #update()} 推进动画。
 */
public interface Animator {
    void beginLand();

    void beginGlide();

    void beginIdle();

    void beginLift();

    void beginFall();

    void update();
}
