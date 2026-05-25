package cc.lvjia.wings.server.config;

public final class WingsConfigDefaults {
    public static final boolean ALLOW_UNDERWATER_FLIGHT = false;

    public static final int FLIGHT_TAKEOFF_GRACE_TICKS_MIN = 0;
    public static final int FLIGHT_TAKEOFF_GRACE_TICKS_MAX = 200;
    public static final int FLIGHT_VIOLATION_LIMIT_MIN = 1;
    public static final int FLIGHT_VIOLATION_LIMIT_MAX = 50;
    public static final int FLIGHT_CORRECTION_COOLDOWN_TICKS_MIN = 0;
    public static final int FLIGHT_CORRECTION_COOLDOWN_TICKS_MAX = 200;
    public static final double FLIGHT_SOFT_LIMIT_MIN = 0.0D;
    public static final double FLIGHT_SOFT_LIMIT_MAX = 10.0D;
    public static final double FLIGHT_HARD_LIMIT_MIN = 0.0D;
    public static final double FLIGHT_HARD_LIMIT_MAX = 20.0D;
    public static final double FLIGHT_UPWARD_ASSIST_MIN = 0.0D;
    public static final double FLIGHT_UPWARD_ASSIST_MAX = 5.0D;

    public static final FlightAntiCheatSettings FLIGHT_ANTI_CHEAT = new FlightAntiCheatSettings(
            false,
            12,
            8,
            4,
            10,
            2.0D,
            1.95D,
            2.2D,
            3.5D,
            3.2D,
            4.0D,
            1.0D,
            0.9D);

    public static final int WING_MIN_SATIATION = 0;
    public static final int WING_MAX_SATIATION = 20;
    public static final double WING_MIN_EXERTION = 0.0D;
    public static final double WING_MAX_EXERTION = 10.0D;
    public static final WingSettingsData WING_SETTINGS = new WingSettingsData(5, 0.0001D, 2, 0.005D);

    private WingsConfigDefaults() {
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        if (!Double.isFinite(value)) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    public record WingSettingsData(
            int requiredFlightSatiation,
            double flyingExertion,
            int requiredLandSatiation,
            double landingExertion) {
    }
}
