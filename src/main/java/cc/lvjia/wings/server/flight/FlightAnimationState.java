package cc.lvjia.wings.server.flight;

public enum FlightAnimationState {
    IDLE(0, false),
    LIFT(1, true),
    GLIDE(2, true),
    LAND(3, true),
    FALL(4, false);

    private final int id;

    private final boolean periodicallySynced;

    FlightAnimationState(int id, boolean periodicallySynced) {
        this.id = id;
        this.periodicallySynced = periodicallySynced;
    }

    public int id() {
        return this.id;
    }

    public boolean shouldSyncPeriodically() {
        return this.periodicallySynced;
    }

    public static FlightAnimationState byId(int id) {
        for (FlightAnimationState state : values()) {
            if (state.id == id) {
                return state;
            }
        }
        return IDLE;
    }
}
