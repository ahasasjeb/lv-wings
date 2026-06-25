package cc.lvjia.wings.server.flight;

// 飞行动画状态枚举：IDLE → LIFT → GLIDE/LAND/FALL
// periodicallySynced 表示飞行中是否定期向客户端同步该状态（补偿丢包）
public enum FlightAnimationState {
    IDLE(0, false),   // 地面空闲，无需周期同步
    LIFT(1, true),    // 抬升阶段，需要补包
    GLIDE(2, true),   // 滑翔阶段，需要补包
    LAND(3, true),    // 着陆阶段，需要补包
    FALL(4, false);   // 自由落体，无需周期同步

    private final int id;

    private final boolean periodicallySynced;

    FlightAnimationState(int id, boolean periodicallySynced) {
        this.id = id;
        this.periodicallySynced = periodicallySynced;
    }

    // 从网络传输的 int id 还原枚举，兜底返回 IDLE
    public static FlightAnimationState byId(int id) {
        for (FlightAnimationState state : values()) {
            if (state.id == id) {
                return state;
            }
        }
        return IDLE;
    }

    public int id() {
        return this.id;
    }

    public boolean shouldSyncPeriodically() {
        return this.periodicallySynced;
    }
}
