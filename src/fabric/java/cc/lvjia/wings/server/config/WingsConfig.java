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

    public static final class Data {
        public boolean allowUnderwaterFlight = WingsConfigDefaults.ALLOW_UNDERWATER_FLIGHT;
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
        public boolean enabled = WingsConfigDefaults.FLIGHT_ANTI_CHEAT.enabled();
        public int takeoffGraceTicks = WingsConfigDefaults.FLIGHT_ANTI_CHEAT.takeoffGraceTicks();
        public int softViolationLimit = WingsConfigDefaults.FLIGHT_ANTI_CHEAT.softViolationLimit();
        public int hardViolationLimit = WingsConfigDefaults.FLIGHT_ANTI_CHEAT.hardViolationLimit();
        public int correctionCooldownTicks = WingsConfigDefaults.FLIGHT_ANTI_CHEAT.correctionCooldownTicks();
        public double softHorizontalLimit = WingsConfigDefaults.FLIGHT_ANTI_CHEAT.softHorizontalLimit();
        public double softVerticalLimit = WingsConfigDefaults.FLIGHT_ANTI_CHEAT.softVerticalLimit();
        public double softTotalLimit = WingsConfigDefaults.FLIGHT_ANTI_CHEAT.softTotalLimit();
        public double hardHorizontalLimit = WingsConfigDefaults.FLIGHT_ANTI_CHEAT.hardHorizontalLimit();
        public double hardVerticalLimit = WingsConfigDefaults.FLIGHT_ANTI_CHEAT.hardVerticalLimit();
        public double hardTotalLimit = WingsConfigDefaults.FLIGHT_ANTI_CHEAT.hardTotalLimit();
        public double upwardAssistHorizontalThreshold = WingsConfigDefaults.FLIGHT_ANTI_CHEAT.upwardAssistHorizontalThreshold();
        public double upwardAssistMaxBonus = WingsConfigDefaults.FLIGHT_ANTI_CHEAT.upwardAssistMaxBonus();

        void normalize() {
            this.takeoffGraceTicks = WingsConfigDefaults.clamp(this.takeoffGraceTicks,
                    WingsConfigDefaults.FLIGHT_TAKEOFF_GRACE_TICKS_MIN,
                    WingsConfigDefaults.FLIGHT_TAKEOFF_GRACE_TICKS_MAX);
            this.softViolationLimit = WingsConfigDefaults.clamp(this.softViolationLimit,
                    WingsConfigDefaults.FLIGHT_VIOLATION_LIMIT_MIN,
                    WingsConfigDefaults.FLIGHT_VIOLATION_LIMIT_MAX);
            this.hardViolationLimit = WingsConfigDefaults.clamp(this.hardViolationLimit,
                    WingsConfigDefaults.FLIGHT_VIOLATION_LIMIT_MIN,
                    WingsConfigDefaults.FLIGHT_VIOLATION_LIMIT_MAX);
            this.correctionCooldownTicks = WingsConfigDefaults.clamp(this.correctionCooldownTicks,
                    WingsConfigDefaults.FLIGHT_CORRECTION_COOLDOWN_TICKS_MIN,
                    WingsConfigDefaults.FLIGHT_CORRECTION_COOLDOWN_TICKS_MAX);
            this.softHorizontalLimit = WingsConfigDefaults.clamp(this.softHorizontalLimit,
                    WingsConfigDefaults.FLIGHT_SOFT_LIMIT_MIN, WingsConfigDefaults.FLIGHT_SOFT_LIMIT_MAX);
            this.softVerticalLimit = WingsConfigDefaults.clamp(this.softVerticalLimit,
                    WingsConfigDefaults.FLIGHT_SOFT_LIMIT_MIN, WingsConfigDefaults.FLIGHT_SOFT_LIMIT_MAX);
            this.softTotalLimit = WingsConfigDefaults.clamp(this.softTotalLimit,
                    WingsConfigDefaults.FLIGHT_SOFT_LIMIT_MIN, WingsConfigDefaults.FLIGHT_SOFT_LIMIT_MAX);
            this.hardHorizontalLimit = WingsConfigDefaults.clamp(this.hardHorizontalLimit,
                    WingsConfigDefaults.FLIGHT_HARD_LIMIT_MIN, WingsConfigDefaults.FLIGHT_HARD_LIMIT_MAX);
            this.hardVerticalLimit = WingsConfigDefaults.clamp(this.hardVerticalLimit,
                    WingsConfigDefaults.FLIGHT_HARD_LIMIT_MIN, WingsConfigDefaults.FLIGHT_HARD_LIMIT_MAX);
            this.hardTotalLimit = WingsConfigDefaults.clamp(this.hardTotalLimit,
                    WingsConfigDefaults.FLIGHT_HARD_LIMIT_MIN, WingsConfigDefaults.FLIGHT_HARD_LIMIT_MAX);
            this.upwardAssistHorizontalThreshold = WingsConfigDefaults.clamp(this.upwardAssistHorizontalThreshold,
                    WingsConfigDefaults.FLIGHT_UPWARD_ASSIST_MIN, WingsConfigDefaults.FLIGHT_UPWARD_ASSIST_MAX);
            this.upwardAssistMaxBonus = WingsConfigDefaults.clamp(this.upwardAssistMaxBonus,
                    WingsConfigDefaults.FLIGHT_UPWARD_ASSIST_MIN, WingsConfigDefaults.FLIGHT_UPWARD_ASSIST_MAX);
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
