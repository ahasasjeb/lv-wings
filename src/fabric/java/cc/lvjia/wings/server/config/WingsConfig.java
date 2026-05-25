package cc.lvjia.wings.server.config;

@SuppressWarnings("null")
public final class WingsConfig {
    private static volatile Data DATA = Data.defaults();
    private static volatile FlightAntiCheatSettings FLIGHT_ANTI_CHEAT_SETTINGS = DATA.flightAntiCheat.toSettings();

    private WingsConfig() {
    }

    public static boolean isUnderwaterFlightAllowed() {
        return DATA.allowUnderwaterFlight;
    }

    public static FlightAntiCheatSettings getFlightAntiCheatSettings() {
        return FLIGHT_ANTI_CHEAT_SETTINGS;
    }

    public static void validate() {
        DATA = ConfigFiles.load("wings-common.json", Data.class, Data::defaults, Data::normalize);
        FLIGHT_ANTI_CHEAT_SETTINGS = DATA.flightAntiCheat.toSettings();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        if (!Double.isFinite(value)) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    public record FlightAntiCheatSettings(
            boolean enabled,
            int takeoffGraceTicks,
            int softViolationLimit,
            int hardViolationLimit,
            int correctionCooldownTicks,
            double softHorizontalLimit,
            double softVerticalLimit,
            double softTotalLimit,
            double hardHorizontalLimit,
            double hardVerticalLimit,
            double hardTotalLimit,
            double upwardAssistHorizontalThreshold,
            double upwardAssistMaxBonus) {
    }

    public static final class Data {
        public boolean allowUnderwaterFlight = false;
        public AntiCheatData flightAntiCheat = new AntiCheatData();

        static Data defaults() {
            return new Data();
        }

        Data normalize() {
            if (this.flightAntiCheat == null) {
                this.flightAntiCheat = new AntiCheatData();
            }
            this.flightAntiCheat.normalize();
            return this;
        }
    }

    public static final class AntiCheatData {
        public boolean enabled = false;
        public int takeoffGraceTicks = 12;
        public int softViolationLimit = 8;
        public int hardViolationLimit = 4;
        public int correctionCooldownTicks = 10;
        public double softHorizontalLimit = 2.0D;
        public double softVerticalLimit = 1.95D;
        public double softTotalLimit = 2.2D;
        public double hardHorizontalLimit = 3.5D;
        public double hardVerticalLimit = 3.2D;
        public double hardTotalLimit = 4.0D;
        public double upwardAssistHorizontalThreshold = 1.0D;
        public double upwardAssistMaxBonus = 0.9D;

        void normalize() {
            this.takeoffGraceTicks = clamp(this.takeoffGraceTicks, 0, 200);
            this.softViolationLimit = clamp(this.softViolationLimit, 1, 50);
            this.hardViolationLimit = clamp(this.hardViolationLimit, 1, 50);
            this.correctionCooldownTicks = clamp(this.correctionCooldownTicks, 0, 200);
            this.softHorizontalLimit = clamp(this.softHorizontalLimit, 0.0D, 10.0D);
            this.softVerticalLimit = clamp(this.softVerticalLimit, 0.0D, 10.0D);
            this.softTotalLimit = clamp(this.softTotalLimit, 0.0D, 10.0D);
            this.hardHorizontalLimit = clamp(this.hardHorizontalLimit, 0.0D, 20.0D);
            this.hardVerticalLimit = clamp(this.hardVerticalLimit, 0.0D, 20.0D);
            this.hardTotalLimit = clamp(this.hardTotalLimit, 0.0D, 20.0D);
            this.upwardAssistHorizontalThreshold = clamp(this.upwardAssistHorizontalThreshold, 0.0D, 5.0D);
            this.upwardAssistMaxBonus = clamp(this.upwardAssistMaxBonus, 0.0D, 5.0D);
        }

        FlightAntiCheatSettings toSettings() {
            return new FlightAntiCheatSettings(
                    this.enabled,
                    this.takeoffGraceTicks,
                    this.softViolationLimit,
                    this.hardViolationLimit,
                    this.correctionCooldownTicks,
                    this.softHorizontalLimit,
                    this.softVerticalLimit,
                    this.softTotalLimit,
                    this.hardHorizontalLimit,
                    this.hardVerticalLimit,
                    this.hardTotalLimit,
                    this.upwardAssistHorizontalThreshold,
                    this.upwardAssistMaxBonus);
        }
    }
}
