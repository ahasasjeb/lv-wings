package cc.lvjia.wings.server.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class WingsConfig {
    public static final ModConfigSpec SPEC;
    private static final Logger LOGGER = LogManager.getLogger("WingsConfig");

    private static final ModConfigSpec.BooleanValue ALLOW_UNDERWATER_FLIGHT;

    private static final ModConfigSpec.BooleanValue ENABLE_FLIGHT_ANTI_CHEAT;
    private static final ModConfigSpec.IntValue TAKEOFF_GRACE_TICKS;
    private static final ModConfigSpec.IntValue SOFT_VIOLATION_LIMIT;
    private static final ModConfigSpec.IntValue HARD_VIOLATION_LIMIT;
    private static final ModConfigSpec.IntValue CORRECTION_COOLDOWN_TICKS;

    private static final ModConfigSpec.DoubleValue SOFT_HORIZONTAL_LIMIT;
    private static final ModConfigSpec.DoubleValue SOFT_VERTICAL_LIMIT;
    private static final ModConfigSpec.DoubleValue SOFT_TOTAL_LIMIT;

    private static final ModConfigSpec.DoubleValue HARD_HORIZONTAL_LIMIT;
    private static final ModConfigSpec.DoubleValue HARD_VERTICAL_LIMIT;
    private static final ModConfigSpec.DoubleValue HARD_TOTAL_LIMIT;

    private static final ModConfigSpec.DoubleValue UPWARD_ASSIST_HORIZONTAL_THRESHOLD;
    private static final ModConfigSpec.DoubleValue UPWARD_ASSIST_MAX_BONUS;

    private static volatile FlightAntiCheatSettings FLIGHT_ANTI_CHEAT_SETTINGS = WingsConfigDefaults.FLIGHT_ANTI_CHEAT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("General configuration for lv wings").push("general");

        ALLOW_UNDERWATER_FLIGHT = builder
            .comment("Whether players can fly while underwater. Disabled by default.")
            .define("allowUnderwaterFlight", WingsConfigDefaults.ALLOW_UNDERWATER_FLIGHT);

        builder.pop();

        builder.comment("Server-side anti-cheat settings for wings flight").push("flightAntiCheat");

        ENABLE_FLIGHT_ANTI_CHEAT = builder
            .comment("Enable wings flight anti-cheat on server side. Default: false")
            .define("enabled", WingsConfigDefaults.FLIGHT_ANTI_CHEAT.enabled());

        TAKEOFF_GRACE_TICKS = builder
            .comment("Grace ticks after takeoff before speed checks are enforced.")
            .defineInRange("takeoffGraceTicks", WingsConfigDefaults.FLIGHT_ANTI_CHEAT.takeoffGraceTicks(),
                    WingsConfigDefaults.FLIGHT_TAKEOFF_GRACE_TICKS_MIN,
                    WingsConfigDefaults.FLIGHT_TAKEOFF_GRACE_TICKS_MAX);

        SOFT_VIOLATION_LIMIT = builder
            .comment("How many soft violations trigger correction.")
            .defineInRange("softViolationLimit", WingsConfigDefaults.FLIGHT_ANTI_CHEAT.softViolationLimit(),
                    WingsConfigDefaults.FLIGHT_VIOLATION_LIMIT_MIN,
                    WingsConfigDefaults.FLIGHT_VIOLATION_LIMIT_MAX);

        HARD_VIOLATION_LIMIT = builder
            .comment("How many hard violations trigger correction.")
            .defineInRange("hardViolationLimit", WingsConfigDefaults.FLIGHT_ANTI_CHEAT.hardViolationLimit(),
                    WingsConfigDefaults.FLIGHT_VIOLATION_LIMIT_MIN,
                    WingsConfigDefaults.FLIGHT_VIOLATION_LIMIT_MAX);

        CORRECTION_COOLDOWN_TICKS = builder
            .comment("Cooldown ticks between corrections.")
            .defineInRange("correctionCooldownTicks", WingsConfigDefaults.FLIGHT_ANTI_CHEAT.correctionCooldownTicks(),
                    WingsConfigDefaults.FLIGHT_CORRECTION_COOLDOWN_TICKS_MIN,
                    WingsConfigDefaults.FLIGHT_CORRECTION_COOLDOWN_TICKS_MAX);

        SOFT_HORIZONTAL_LIMIT = builder
            .comment("Soft horizontal movement limit.")
            .defineInRange("softHorizontalLimit", WingsConfigDefaults.FLIGHT_ANTI_CHEAT.softHorizontalLimit(),
                    WingsConfigDefaults.FLIGHT_SOFT_LIMIT_MIN, WingsConfigDefaults.FLIGHT_SOFT_LIMIT_MAX);

        SOFT_VERTICAL_LIMIT = builder
            .comment("Soft upward vertical movement limit.")
            .defineInRange("softVerticalLimit", WingsConfigDefaults.FLIGHT_ANTI_CHEAT.softVerticalLimit(),
                    WingsConfigDefaults.FLIGHT_SOFT_LIMIT_MIN, WingsConfigDefaults.FLIGHT_SOFT_LIMIT_MAX);

        SOFT_TOTAL_LIMIT = builder
            .comment("Soft total movement limit.")
            .defineInRange("softTotalLimit", WingsConfigDefaults.FLIGHT_ANTI_CHEAT.softTotalLimit(),
                    WingsConfigDefaults.FLIGHT_SOFT_LIMIT_MIN, WingsConfigDefaults.FLIGHT_SOFT_LIMIT_MAX);

        HARD_HORIZONTAL_LIMIT = builder
            .comment("Hard horizontal movement limit.")
            .defineInRange("hardHorizontalLimit", WingsConfigDefaults.FLIGHT_ANTI_CHEAT.hardHorizontalLimit(),
                    WingsConfigDefaults.FLIGHT_HARD_LIMIT_MIN, WingsConfigDefaults.FLIGHT_HARD_LIMIT_MAX);

        HARD_VERTICAL_LIMIT = builder
            .comment("Hard upward vertical movement limit.")
            .defineInRange("hardVerticalLimit", WingsConfigDefaults.FLIGHT_ANTI_CHEAT.hardVerticalLimit(),
                    WingsConfigDefaults.FLIGHT_HARD_LIMIT_MIN, WingsConfigDefaults.FLIGHT_HARD_LIMIT_MAX);

        HARD_TOTAL_LIMIT = builder
            .comment("Hard total movement limit.")
            .defineInRange("hardTotalLimit", WingsConfigDefaults.FLIGHT_ANTI_CHEAT.hardTotalLimit(),
                    WingsConfigDefaults.FLIGHT_HARD_LIMIT_MIN, WingsConfigDefaults.FLIGHT_HARD_LIMIT_MAX);

        UPWARD_ASSIST_HORIZONTAL_THRESHOLD = builder
            .comment("When horizontal speed is below this value, upward limits gain extra tolerance.")
            .defineInRange("upwardAssistHorizontalThreshold",
                    WingsConfigDefaults.FLIGHT_ANTI_CHEAT.upwardAssistHorizontalThreshold(),
                    WingsConfigDefaults.FLIGHT_UPWARD_ASSIST_MIN, WingsConfigDefaults.FLIGHT_UPWARD_ASSIST_MAX);

        UPWARD_ASSIST_MAX_BONUS = builder
            .comment("Maximum extra upward tolerance applied at very low horizontal speed.")
            .defineInRange("upwardAssistMaxBonus", WingsConfigDefaults.FLIGHT_ANTI_CHEAT.upwardAssistMaxBonus(),
                    WingsConfigDefaults.FLIGHT_UPWARD_ASSIST_MIN, WingsConfigDefaults.FLIGHT_UPWARD_ASSIST_MAX);

        builder.pop();
        SPEC = builder.build();
    }

    private WingsConfig() {
    }

    public static boolean isUnderwaterFlightAllowed() {
        Boolean allow = ALLOW_UNDERWATER_FLIGHT.get();
        if (allow == null) {
            LOGGER.warn("Underwater flight flag is null. Reverting to default {}.",
                    WingsConfigDefaults.ALLOW_UNDERWATER_FLIGHT);
            ALLOW_UNDERWATER_FLIGHT.set(WingsConfigDefaults.ALLOW_UNDERWATER_FLIGHT);
            return WingsConfigDefaults.ALLOW_UNDERWATER_FLIGHT;
        }
        return allow;
    }

    public static FlightAntiCheatSettings getFlightAntiCheatSettings() {
        return FLIGHT_ANTI_CHEAT_SETTINGS;
    }

    public static void validate() {
        isUnderwaterFlightAllowed();
        FLIGHT_ANTI_CHEAT_SETTINGS = loadFlightAntiCheatSettings();
    }

    private static FlightAntiCheatSettings loadFlightAntiCheatSettings() {
        return new FlightAntiCheatSettings(
                readBoolean(ENABLE_FLIGHT_ANTI_CHEAT, WingsConfigDefaults.FLIGHT_ANTI_CHEAT.enabled(), "enabled"),
                readInt(TAKEOFF_GRACE_TICKS, WingsConfigDefaults.FLIGHT_ANTI_CHEAT.takeoffGraceTicks(),
                        "takeoffGraceTicks"),
                readInt(SOFT_VIOLATION_LIMIT, WingsConfigDefaults.FLIGHT_ANTI_CHEAT.softViolationLimit(),
                        "softViolationLimit"),
                readInt(HARD_VIOLATION_LIMIT, WingsConfigDefaults.FLIGHT_ANTI_CHEAT.hardViolationLimit(),
                        "hardViolationLimit"),
                readInt(CORRECTION_COOLDOWN_TICKS, WingsConfigDefaults.FLIGHT_ANTI_CHEAT.correctionCooldownTicks(),
                        "correctionCooldownTicks"),
                readDouble(SOFT_HORIZONTAL_LIMIT, WingsConfigDefaults.FLIGHT_ANTI_CHEAT.softHorizontalLimit(),
                        "softHorizontalLimit"),
                readDouble(SOFT_VERTICAL_LIMIT, WingsConfigDefaults.FLIGHT_ANTI_CHEAT.softVerticalLimit(),
                        "softVerticalLimit"),
                readDouble(SOFT_TOTAL_LIMIT, WingsConfigDefaults.FLIGHT_ANTI_CHEAT.softTotalLimit(),
                        "softTotalLimit"),
                readDouble(HARD_HORIZONTAL_LIMIT, WingsConfigDefaults.FLIGHT_ANTI_CHEAT.hardHorizontalLimit(),
                        "hardHorizontalLimit"),
                readDouble(HARD_VERTICAL_LIMIT, WingsConfigDefaults.FLIGHT_ANTI_CHEAT.hardVerticalLimit(),
                        "hardVerticalLimit"),
                readDouble(HARD_TOTAL_LIMIT, WingsConfigDefaults.FLIGHT_ANTI_CHEAT.hardTotalLimit(),
                        "hardTotalLimit"),
                readDouble(UPWARD_ASSIST_HORIZONTAL_THRESHOLD,
                        WingsConfigDefaults.FLIGHT_ANTI_CHEAT.upwardAssistHorizontalThreshold(),
                        "upwardAssistHorizontalThreshold"),
                readDouble(UPWARD_ASSIST_MAX_BONUS, WingsConfigDefaults.FLIGHT_ANTI_CHEAT.upwardAssistMaxBonus(),
                        "upwardAssistMaxBonus")
        );
    }

    private static boolean readBoolean(ModConfigSpec.BooleanValue value, boolean defaultValue, String key) {
        Boolean current = value.get();
        if (current != null) {
            return current;
        }
        LOGGER.warn("Config '{}' is null. Reverting to default {}.", key, defaultValue);
        value.set(defaultValue);
        return defaultValue;
    }

    private static int readInt(ModConfigSpec.IntValue value, int defaultValue, String key) {
        Integer current = value.get();
        if (current != null) {
            return current;
        }
        LOGGER.warn("Config '{}' is null. Reverting to default {}.", key, defaultValue);
        value.set(defaultValue);
        return defaultValue;
    }

    private static double readDouble(ModConfigSpec.DoubleValue value, double defaultValue, String key) {
        Double current = value.get();
        if (current != null) {
            return current;
        }
        LOGGER.warn("Config '{}' is null. Reverting to default {}.", key, defaultValue);
        value.set(defaultValue);
        return defaultValue;
    }

}
