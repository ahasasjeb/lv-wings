package cc.lvjia.wings.server.flight;

import cc.lvjia.wings.server.config.FlightAntiCheatSettings;

final class FlightMovementChecks {
    private FlightMovementChecks() {
    }

    static boolean exceedsDownwardLimit(double verticalMovement, double limit) {
        return verticalMovement < -limit;
    }

    static double computeUpwardVerticalBonus(double horizontal, FlightAntiCheatSettings settings) {
        double threshold = settings.upwardAssistHorizontalThreshold();
        double maxBonus = settings.upwardAssistMaxBonus();
        if (threshold <= 0.0D || maxBonus <= 0.0D) {
            return 0.0D;
        }
        return maxBonus * (1.0D - Math.min(1.0D, horizontal / threshold));
    }
}
