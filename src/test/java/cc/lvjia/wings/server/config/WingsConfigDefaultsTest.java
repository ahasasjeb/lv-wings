package cc.lvjia.wings.server.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class WingsConfigDefaultsTest {
    @Test
    void clampsIntValuesToProvidedRange() {
        assertEquals(0, WingsConfigDefaults.clamp(-1, 0, 10));
        assertEquals(5, WingsConfigDefaults.clamp(5, 0, 10));
        assertEquals(10, WingsConfigDefaults.clamp(11, 0, 10));
    }

    @Test
    void clampsDoubleValuesToProvidedRange() {
        assertEquals(0.0D, WingsConfigDefaults.clamp(-0.5D, 0.0D, 1.0D));
        assertEquals(0.5D, WingsConfigDefaults.clamp(0.5D, 0.0D, 1.0D));
        assertEquals(1.0D, WingsConfigDefaults.clamp(1.5D, 0.0D, 1.0D));
    }

    @Test
    void nonFiniteDoubleValuesFallBackToMinimum() {
        assertEquals(0.0D, WingsConfigDefaults.clamp(Double.NaN, 0.0D, 1.0D));
        assertEquals(0.0D, WingsConfigDefaults.clamp(Double.POSITIVE_INFINITY, 0.0D, 1.0D));
        assertEquals(0.0D, WingsConfigDefaults.clamp(Double.NEGATIVE_INFINITY, 0.0D, 1.0D));
    }

    @Test
    void defaultFlightAntiCheatSettingsStayInsidePublishedRanges() {
        FlightAntiCheatSettings settings = WingsConfigDefaults.FLIGHT_ANTI_CHEAT;

        assertFalse(settings.enabled());
        assertBetween(settings.takeoffGraceTicks(),
                WingsConfigDefaults.FLIGHT_TAKEOFF_GRACE_TICKS_MIN,
                WingsConfigDefaults.FLIGHT_TAKEOFF_GRACE_TICKS_MAX);
        assertBetween(settings.softViolationLimit(),
                WingsConfigDefaults.FLIGHT_VIOLATION_LIMIT_MIN,
                WingsConfigDefaults.FLIGHT_VIOLATION_LIMIT_MAX);
        assertBetween(settings.hardViolationLimit(),
                WingsConfigDefaults.FLIGHT_VIOLATION_LIMIT_MIN,
                WingsConfigDefaults.FLIGHT_VIOLATION_LIMIT_MAX);
        assertBetween(settings.correctionCooldownTicks(),
                WingsConfigDefaults.FLIGHT_CORRECTION_COOLDOWN_TICKS_MIN,
                WingsConfigDefaults.FLIGHT_CORRECTION_COOLDOWN_TICKS_MAX);
        assertBetween(settings.softHorizontalLimit(),
                WingsConfigDefaults.FLIGHT_SOFT_LIMIT_MIN,
                WingsConfigDefaults.FLIGHT_SOFT_LIMIT_MAX);
        assertBetween(settings.softVerticalLimit(),
                WingsConfigDefaults.FLIGHT_SOFT_LIMIT_MIN,
                WingsConfigDefaults.FLIGHT_SOFT_LIMIT_MAX);
        assertBetween(settings.softTotalLimit(),
                WingsConfigDefaults.FLIGHT_SOFT_LIMIT_MIN,
                WingsConfigDefaults.FLIGHT_SOFT_LIMIT_MAX);
        assertBetween(settings.hardHorizontalLimit(),
                WingsConfigDefaults.FLIGHT_HARD_LIMIT_MIN,
                WingsConfigDefaults.FLIGHT_HARD_LIMIT_MAX);
        assertBetween(settings.hardVerticalLimit(),
                WingsConfigDefaults.FLIGHT_HARD_LIMIT_MIN,
                WingsConfigDefaults.FLIGHT_HARD_LIMIT_MAX);
        assertBetween(settings.hardTotalLimit(),
                WingsConfigDefaults.FLIGHT_HARD_LIMIT_MIN,
                WingsConfigDefaults.FLIGHT_HARD_LIMIT_MAX);
        assertBetween(settings.upwardAssistHorizontalThreshold(),
                WingsConfigDefaults.FLIGHT_UPWARD_ASSIST_MIN,
                WingsConfigDefaults.FLIGHT_UPWARD_ASSIST_MAX);
        assertBetween(settings.upwardAssistMaxBonus(),
                WingsConfigDefaults.FLIGHT_UPWARD_ASSIST_MIN,
                WingsConfigDefaults.FLIGHT_UPWARD_ASSIST_MAX);
    }

    @Test
    void defaultWingSettingsStayInsidePublishedRanges() {
        WingsConfigDefaults.WingSettingsData settings = WingsConfigDefaults.WING_SETTINGS;

        assertBetween(settings.requiredFlightSatiation(),
                WingsConfigDefaults.WING_MIN_SATIATION,
                WingsConfigDefaults.WING_MAX_SATIATION);
        assertBetween(settings.requiredLandSatiation(),
                WingsConfigDefaults.WING_MIN_SATIATION,
                WingsConfigDefaults.WING_MAX_SATIATION);
        assertBetween(settings.flyingExertion(),
                WingsConfigDefaults.WING_MIN_EXERTION,
                WingsConfigDefaults.WING_MAX_EXERTION);
        assertBetween(settings.landingExertion(),
                WingsConfigDefaults.WING_MIN_EXERTION,
                WingsConfigDefaults.WING_MAX_EXERTION);
    }

    private static void assertBetween(int value, int min, int max) {
        assertEquals(value, WingsConfigDefaults.clamp(value, min, max));
    }

    private static void assertBetween(double value, double min, double max) {
        assertEquals(value, WingsConfigDefaults.clamp(value, min, max));
    }
}
