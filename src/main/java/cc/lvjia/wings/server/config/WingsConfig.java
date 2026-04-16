package cc.lvjia.wings.server.config;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WingsConfig {
    public static final ModConfigSpec SPEC;
    private static final Logger LOGGER = LogManager.getLogger("WingsConfig");

    private static final List<String> DEFAULT_WEAR_OBSTRUCTIONS = List.of("minecraft:elytra");
    private static final ModConfigSpec.ConfigValue<List<? extends String>> WEAR_OBSTRUCTIONS;

    private static final boolean DEFAULT_ALLOW_UNDERWATER_FLIGHT = false;
    private static final ModConfigSpec.BooleanValue ALLOW_UNDERWATER_FLIGHT;

    private static final boolean DEFAULT_ENABLE_FLIGHT_ANTI_CHEAT = false;
    private static final int DEFAULT_TAKEOFF_GRACE_TICKS = 12;
    private static final int DEFAULT_SOFT_VIOLATION_LIMIT = 8;
    private static final int DEFAULT_HARD_VIOLATION_LIMIT = 4;
    private static final int DEFAULT_CORRECTION_COOLDOWN_TICKS = 10;

    private static final double DEFAULT_SOFT_HORIZONTAL_LIMIT = 2.0D;
    private static final double DEFAULT_SOFT_VERTICAL_LIMIT = 1.95D;
    private static final double DEFAULT_SOFT_TOTAL_LIMIT = 2.2D;

    private static final double DEFAULT_HARD_HORIZONTAL_LIMIT = 3.5D;
    private static final double DEFAULT_HARD_VERTICAL_LIMIT = 3.2D;
    private static final double DEFAULT_HARD_TOTAL_LIMIT = 4.0D;

    private static final double DEFAULT_UPWARD_ASSIST_HORIZONTAL_THRESHOLD = 1.0D;
    private static final double DEFAULT_UPWARD_ASSIST_MAX_BONUS = 0.9D;

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

    private static volatile FlightAntiCheatSettings FLIGHT_ANTI_CHEAT_SETTINGS = defaultFlightAntiCheatSettings();

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("General configuration for lv wings").push("general");

        WEAR_OBSTRUCTIONS = builder
                .comment("List of item IDs that prevent players from equipping wings.")
                .defineListAllowEmpty("wearObstructions", DEFAULT_WEAR_OBSTRUCTIONS, () -> "", value -> value instanceof String && Identifier.tryParse((String) value) != null);

        ALLOW_UNDERWATER_FLIGHT = builder
            .comment("Whether players can fly while underwater. Disabled by default.")
            .define("allowUnderwaterFlight", DEFAULT_ALLOW_UNDERWATER_FLIGHT);

        builder.pop();

        builder.comment("Server-side anti-cheat settings for wings flight").push("flightAntiCheat");

        ENABLE_FLIGHT_ANTI_CHEAT = builder
            .comment("Enable wings flight anti-cheat on server side. Default: false")
            .define("enabled", DEFAULT_ENABLE_FLIGHT_ANTI_CHEAT);

        TAKEOFF_GRACE_TICKS = builder
            .comment("Grace ticks after takeoff before speed checks are enforced.")
            .defineInRange("takeoffGraceTicks", DEFAULT_TAKEOFF_GRACE_TICKS, 0, 200);

        SOFT_VIOLATION_LIMIT = builder
            .comment("How many soft violations trigger correction.")
            .defineInRange("softViolationLimit", DEFAULT_SOFT_VIOLATION_LIMIT, 1, 50);

        HARD_VIOLATION_LIMIT = builder
            .comment("How many hard violations trigger correction.")
            .defineInRange("hardViolationLimit", DEFAULT_HARD_VIOLATION_LIMIT, 1, 50);

        CORRECTION_COOLDOWN_TICKS = builder
            .comment("Cooldown ticks between corrections.")
            .defineInRange("correctionCooldownTicks", DEFAULT_CORRECTION_COOLDOWN_TICKS, 0, 200);

        SOFT_HORIZONTAL_LIMIT = builder
            .comment("Soft horizontal movement limit.")
            .defineInRange("softHorizontalLimit", DEFAULT_SOFT_HORIZONTAL_LIMIT, 0.0D, 10.0D);

        SOFT_VERTICAL_LIMIT = builder
            .comment("Soft upward vertical movement limit.")
            .defineInRange("softVerticalLimit", DEFAULT_SOFT_VERTICAL_LIMIT, 0.0D, 10.0D);

        SOFT_TOTAL_LIMIT = builder
            .comment("Soft total movement limit.")
            .defineInRange("softTotalLimit", DEFAULT_SOFT_TOTAL_LIMIT, 0.0D, 10.0D);

        HARD_HORIZONTAL_LIMIT = builder
            .comment("Hard horizontal movement limit.")
            .defineInRange("hardHorizontalLimit", DEFAULT_HARD_HORIZONTAL_LIMIT, 0.0D, 20.0D);

        HARD_VERTICAL_LIMIT = builder
            .comment("Hard upward vertical movement limit.")
            .defineInRange("hardVerticalLimit", DEFAULT_HARD_VERTICAL_LIMIT, 0.0D, 20.0D);

        HARD_TOTAL_LIMIT = builder
            .comment("Hard total movement limit.")
            .defineInRange("hardTotalLimit", DEFAULT_HARD_TOTAL_LIMIT, 0.0D, 20.0D);

        UPWARD_ASSIST_HORIZONTAL_THRESHOLD = builder
            .comment("When horizontal speed is below this value, upward limits gain extra tolerance.")
            .defineInRange("upwardAssistHorizontalThreshold", DEFAULT_UPWARD_ASSIST_HORIZONTAL_THRESHOLD, 0.0D, 5.0D);

        UPWARD_ASSIST_MAX_BONUS = builder
            .comment("Maximum extra upward tolerance applied at very low horizontal speed.")
            .defineInRange("upwardAssistMaxBonus", DEFAULT_UPWARD_ASSIST_MAX_BONUS, 0.0D, 5.0D);

        builder.pop();
        SPEC = builder.build();
    }

    private WingsConfig() {
    }

    public static List<String> getWearObstructions() {
        List<? extends String> entries = WEAR_OBSTRUCTIONS.get();
        if (entries == null) {
            LOGGER.warn("Wear obstructions list is null. Reverting to defaults {}.", DEFAULT_WEAR_OBSTRUCTIONS);
            WEAR_OBSTRUCTIONS.set(DEFAULT_WEAR_OBSTRUCTIONS);
            return Collections.unmodifiableList(new ArrayList<>(DEFAULT_WEAR_OBSTRUCTIONS));
        }
        List<String> sanitized = new ArrayList<>();
        for (Object entryObj : entries) {
            if (entryObj == null) {
                continue;
            }
            String entry = entryObj.toString().trim();
            if (entry.isEmpty()) {
                continue;
            }
            if (Identifier.tryParse(entry) == null) {
                LOGGER.warn("Ignoring invalid wear obstruction id '{}'. Expected a namespaced id such as 'minecraft:elytra'.", entry);
                continue;
            }
            sanitized.add(entry);
        }

        if (sanitized.isEmpty()) {
            LOGGER.warn("No valid wear obstruction entries found. Reverting to defaults {}.", DEFAULT_WEAR_OBSTRUCTIONS);
            WEAR_OBSTRUCTIONS.set(DEFAULT_WEAR_OBSTRUCTIONS);
            sanitized.addAll(DEFAULT_WEAR_OBSTRUCTIONS);
        }

        return Collections.unmodifiableList(sanitized);
    }

    public static String[] getWearObstructionsArray() {
        return getWearObstructions().toArray(String[]::new);
    }

    public static boolean isUnderwaterFlightAllowed() {
        Boolean allow = ALLOW_UNDERWATER_FLIGHT.get();
        if (allow == null) {
            LOGGER.warn("Underwater flight flag is null. Reverting to default {}.", DEFAULT_ALLOW_UNDERWATER_FLIGHT);
            ALLOW_UNDERWATER_FLIGHT.set(DEFAULT_ALLOW_UNDERWATER_FLIGHT);
            return DEFAULT_ALLOW_UNDERWATER_FLIGHT;
        }
        return allow;
    }

    public static FlightAntiCheatSettings getFlightAntiCheatSettings() {
        return FLIGHT_ANTI_CHEAT_SETTINGS;
    }

    public static void validate() {
        getWearObstructions();
        isUnderwaterFlightAllowed();
        FLIGHT_ANTI_CHEAT_SETTINGS = loadFlightAntiCheatSettings();
    }

    private static FlightAntiCheatSettings loadFlightAntiCheatSettings() {
        return new FlightAntiCheatSettings(
                readBoolean(ENABLE_FLIGHT_ANTI_CHEAT, DEFAULT_ENABLE_FLIGHT_ANTI_CHEAT, "enabled"),
                readInt(TAKEOFF_GRACE_TICKS, DEFAULT_TAKEOFF_GRACE_TICKS, "takeoffGraceTicks"),
                readInt(SOFT_VIOLATION_LIMIT, DEFAULT_SOFT_VIOLATION_LIMIT, "softViolationLimit"),
                readInt(HARD_VIOLATION_LIMIT, DEFAULT_HARD_VIOLATION_LIMIT, "hardViolationLimit"),
                readInt(CORRECTION_COOLDOWN_TICKS, DEFAULT_CORRECTION_COOLDOWN_TICKS, "correctionCooldownTicks"),
                readDouble(SOFT_HORIZONTAL_LIMIT, DEFAULT_SOFT_HORIZONTAL_LIMIT, "softHorizontalLimit"),
                readDouble(SOFT_VERTICAL_LIMIT, DEFAULT_SOFT_VERTICAL_LIMIT, "softVerticalLimit"),
                readDouble(SOFT_TOTAL_LIMIT, DEFAULT_SOFT_TOTAL_LIMIT, "softTotalLimit"),
                readDouble(HARD_HORIZONTAL_LIMIT, DEFAULT_HARD_HORIZONTAL_LIMIT, "hardHorizontalLimit"),
                readDouble(HARD_VERTICAL_LIMIT, DEFAULT_HARD_VERTICAL_LIMIT, "hardVerticalLimit"),
                readDouble(HARD_TOTAL_LIMIT, DEFAULT_HARD_TOTAL_LIMIT, "hardTotalLimit"),
                readDouble(UPWARD_ASSIST_HORIZONTAL_THRESHOLD, DEFAULT_UPWARD_ASSIST_HORIZONTAL_THRESHOLD, "upwardAssistHorizontalThreshold"),
                readDouble(UPWARD_ASSIST_MAX_BONUS, DEFAULT_UPWARD_ASSIST_MAX_BONUS, "upwardAssistMaxBonus")
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

    private static FlightAntiCheatSettings defaultFlightAntiCheatSettings() {
        return new FlightAntiCheatSettings(
                DEFAULT_ENABLE_FLIGHT_ANTI_CHEAT,
                DEFAULT_TAKEOFF_GRACE_TICKS,
                DEFAULT_SOFT_VIOLATION_LIMIT,
                DEFAULT_HARD_VIOLATION_LIMIT,
                DEFAULT_CORRECTION_COOLDOWN_TICKS,
                DEFAULT_SOFT_HORIZONTAL_LIMIT,
                DEFAULT_SOFT_VERTICAL_LIMIT,
                DEFAULT_SOFT_TOTAL_LIMIT,
                DEFAULT_HARD_HORIZONTAL_LIMIT,
                DEFAULT_HARD_VERTICAL_LIMIT,
                DEFAULT_HARD_TOTAL_LIMIT,
                DEFAULT_UPWARD_ASSIST_HORIZONTAL_THRESHOLD,
                DEFAULT_UPWARD_ASSIST_MAX_BONUS
        );
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
            double upwardAssistMaxBonus
    ) {
    }
}
