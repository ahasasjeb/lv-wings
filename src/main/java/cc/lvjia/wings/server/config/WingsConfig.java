package cc.lvjia.wings.server.config;

import java.util.List;

public final class WingsConfig {
    private static final List<String> DEFAULT_WEAR_OBSTRUCTIONS = List.of("minecraft:elytra");
    private static final boolean DEFAULT_ALLOW_UNDERWATER_FLIGHT = false;
    private static final FlightAntiCheatSettings FLIGHT_ANTI_CHEAT_SETTINGS = new FlightAntiCheatSettings(
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
            0.9D
    );

    private WingsConfig() {
    }

    public static List<String> getWearObstructions() {
        return DEFAULT_WEAR_OBSTRUCTIONS;
    }

    public static String[] getWearObstructionsArray() {
        return DEFAULT_WEAR_OBSTRUCTIONS.toArray(String[]::new);
    }

    public static boolean isUnderwaterFlightAllowed() {
        return DEFAULT_ALLOW_UNDERWATER_FLIGHT;
    }

    public static FlightAntiCheatSettings getFlightAntiCheatSettings() {
        return FLIGHT_ANTI_CHEAT_SETTINGS;
    }

    public static void validate() {
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
