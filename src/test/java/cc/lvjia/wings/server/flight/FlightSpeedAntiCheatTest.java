package cc.lvjia.wings.server.flight;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightSpeedAntiCheatTest {
    @Test
    void downwardLimitIgnoresNormalTerminalVelocityButDetectsFasterDescent() {
        double softLimit = 4.25D;

        assertFalse(FlightMovementChecks.exceedsDownwardLimit(-3.92D, softLimit));
        assertFalse(FlightMovementChecks.exceedsDownwardLimit(-softLimit, softLimit));
        assertTrue(FlightMovementChecks.exceedsDownwardLimit(-4.5D, softLimit));
    }

    @Test
    void downwardLimitDoesNotAffectUpwardMovement() {
        assertFalse(FlightMovementChecks.exceedsDownwardLimit(8.0D, 4.25D));
    }
}
