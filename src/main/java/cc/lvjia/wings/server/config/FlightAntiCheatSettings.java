package cc.lvjia.wings.server.config;

public record FlightAntiCheatSettings(
        boolean enabled,
        int takeoffGraceTicks,
        int softViolationLimit,
        int hardViolationLimit,
        int correctionCooldownTicks,
        double softHorizontalLimit,
        double softVerticalLimit,
        double softDownwardLimit,
        double softTotalLimit,
        double hardHorizontalLimit,
        double hardVerticalLimit,
        double hardDownwardLimit,
        double hardTotalLimit,
        double upwardAssistHorizontalThreshold,
        double upwardAssistMaxBonus) {
}
