package cc.lvjia.wings.client.hooks;

/** 飞行副手的提取结果随原版渲染状态传递，提交几何时无需访问玩家。 */
public interface EmptyOffhandRenderState {
    boolean wings$renderEmptyOffhand();

    void wings$setRenderEmptyOffhand(boolean render);
}
