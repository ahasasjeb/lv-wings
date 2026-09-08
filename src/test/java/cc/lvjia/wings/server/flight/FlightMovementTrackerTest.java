package cc.lvjia.wings.server.flight;

import cc.lvjia.wings.server.config.FlightAntiCheatSettings;
import cc.lvjia.wings.server.config.WingsConfigDefaults;
import org.junit.jupiter.api.Test;

import static cc.lvjia.wings.server.flight.FlightMovementTracker.Result.*;
import static org.junit.jupiter.api.Assertions.*;

class FlightMovementTrackerTest {
    private static final FlightAntiCheatSettings DEFAULTS = WingsConfigDefaults.FLIGHT_ANTI_CHEAT;

    @Test
    void normalFlightAndTerminalDescentStayClean() {
        FlightMovementTracker tracker = new FlightMovementTracker(0);
        for (int tick = 0; tick < 1_000; tick++) {
            tracker.recordMovement(1.5D, -3.92D, 0.0D);
            assertEquals(CLEAN, finishTick(tracker, tick, DEFAULTS));
        }
    }

    @Test
    void lowHorizontalSpeedKeepsUpwardAssist() {
        FlightMovementTracker tracker = new FlightMovementTracker(0);
        for (int tick = 0; tick < 200; tick++) {
            tracker.recordMovement(0.0D, 2.5D, 0.0D);
            assertEquals(CLEAN, finishTick(tracker, tick, DEFAULTS));
        }
    }

    @Test
    void splittingPacketsCannotHideSustainedOverspeed() {
        FlightMovementTracker whole = new FlightMovementTracker(0);
        FlightMovementTracker split = new FlightMovementTracker(0);
        boolean corrected = false;
        for (int tick = 0; tick < 40; tick++) {
            whole.recordMovement(6.0D, 0.0D, 0.0D);
            for (int packet = 0; packet < 12; packet++) {
                split.recordMovement(0.5D, 0.0D, 0.0D);
            }
            var result = finishTick(whole, tick, DEFAULTS);
            assertEquals(result, finishTick(split, tick, DEFAULTS));
            corrected |= result == CORRECT;
        }
        assertTrue(corrected);
    }

    @Test
    void repeatedThreeTickPacketBurstsAtLegalSpeedStayClean() {
        FlightMovementTracker tracker = new FlightMovementTracker(0);
        for (int tick = 0; tick < 300; tick++) {
            if (tick % 3 == 2) {
                for (int packet = 0; packet < 3; packet++) {
                    tracker.recordMovement(2.0D, 0.0D, 0.0D);
                }
            }
            assertEquals(CLEAN, finishTick(tracker, tick, DEFAULTS));
        }
    }

    @Test
    void aSingleLargeBurstDoesNotBecomeSeveralViolations() {
        FlightMovementTracker tracker = new FlightMovementTracker(0);
        for (int packet = 0; packet < 100; packet++) {
            tracker.recordMovement(1.0D, 0.0D, 0.0D);
        }
        assertEquals(SUSPECT, finishTick(tracker, 0, DEFAULTS));
        assertEquals(UNCHANGED, finishTick(tracker, 0, DEFAULTS));
        for (int tick = 1; tick <= 40; tick++) {
            tracker.recordMovement(1.5D, 0.0D, 0.0D);
            assertEquals(CLEAN, finishTick(tracker, tick, DEFAULTS));
        }
    }

    @Test
    void horizontalAndVerticalReversalsCannotCancelTravel() {
        for (boolean horizontal : new boolean[]{true, false}) {
            FlightMovementTracker tracker = new FlightMovementTracker(0);
            for (int tick = 0; tick < 4; tick++) {
                tracker.recordMovement(horizontal ? 20.0D : 0.0D, horizontal ? 0.0D : 20.0D, 0.0D);
                tracker.recordMovement(horizontal ? -20.0D : 0.0D, horizontal ? 0.0D : -20.0D, 0.0D);
                assertEquals(tick == 3 ? CORRECT : SUSPECT, finishTick(tracker, tick, DEFAULTS));
            }
        }
    }

    @Test
    void sustainedSoftOverspeedAndExcessiveDescentAreCorrected() {
        for (boolean descending : new boolean[]{true, false}) {
            FlightMovementTracker tracker = new FlightMovementTracker(0);
            boolean corrected = false;
            for (int tick = 0; tick < 100; tick++) {
                tracker.recordMovement(descending ? 0.0D : 2.5D, descending ? -5.0D : 0.0D, 0.0D);
                corrected |= finishTick(tracker, tick, DEFAULTS) == CORRECT;
            }
            assertTrue(corrected);
        }
    }

    @Test
    void graceAboveAnimationMaximumStillExpires() {
        FlightAntiCheatSettings settings = withGrace(40);
        FlightMovementTracker tracker = new FlightMovementTracker(0);
        for (int tick = 0; tick < 48; tick++) {
            tracker.recordMovement(2.5D, 0.0D, 0.0D);
            assertEquals(tick < 40 ? CLEAN : tick < 47 ? SUSPECT : CORRECT,
                    finishTick(tracker, tick, settings));
        }
    }

    @Test
    void hardChecksApplyEvenDuringMaximumTakeoffGrace() {
        FlightMovementTracker tracker = new FlightMovementTracker(0);
        for (int tick = 0; tick < 4; tick++) {
            tracker.recordMovement(20.0D, 0.0D, 0.0D);
            assertEquals(tick == 3 ? CORRECT : SUSPECT, finishTick(tracker, tick, withGrace(200)));
        }
    }

    @Test
    void brieflyStoppingDoesNotDiscardEvidenceOrRenewGrace() {
        FlightMovementTracker tracker = new FlightMovementTracker(0);
        for (int tick = 0; tick < 30; tick++) {
            finishTick(tracker, tick, DEFAULTS);
        }
        for (int tick = 30; tick < 38; tick++) {
            // 每隔一个 tick 收翅；违规仍累计，且空闲帧不能把证据清空。
            if (tick % 2 == 0) {
                tracker.markFlying(tick);
                tracker.recordMovement(20.0D, 0.0D, 0.0D);
                assertEquals(tick == 36 ? CORRECT : SUSPECT, finishTick(tracker, tick, DEFAULTS));
            } else {
                assertEquals(CLEAN, finishTick(tracker, tick, DEFAULTS));
            }
            assertFalse(tracker.canDiscard(tick));
        }
        assertTrue(tracker.canDiscard(77));
    }

    @Test
    void safePositionNeedsTwentyCleanTicksToRecover() {
        FlightMovementTracker tracker = new FlightMovementTracker(0);
        tracker.recordMovement(20.0D, 0.0D, 0.0D);
        assertEquals(SUSPECT, finishTick(tracker, 0, DEFAULTS));
        assertFalse(tracker.canAdvanceSafePosition());
        for (int tick = 1; tick < 20; tick++) {
            finishTick(tracker, tick, DEFAULTS);
            assertFalse(tracker.canAdvanceSafePosition());
        }
        finishTick(tracker, 20, DEFAULTS);
        assertTrue(tracker.canAdvanceSafePosition());
    }

    @Test
    void teleportDiscardsCurrentSampleButPreservesEvidenceAndCooldown() {
        FlightMovementTracker tracker = new FlightMovementTracker(0);
        for (int tick = 0; tick < 3; tick++) {
            tracker.recordMovement(20.0D, 0.0D, 0.0D);
            assertEquals(SUSPECT, finishTick(tracker, tick, DEFAULTS));
        }
        tracker.recordMovement(100.0D, 0.0D, 0.0D);
        tracker.rebase();
        assertEquals(CLEAN, finishTick(tracker, 3, DEFAULTS));
        tracker.recordMovement(20.0D, 0.0D, 0.0D);
        assertEquals(CORRECT, finishTick(tracker, 4, DEFAULTS));
        tracker.onCorrection(4, 100);
        tracker.rebase();
        assertTrue(tracker.isCoolingDown(103));
        assertFalse(tracker.canDiscard(103));
        assertFalse(tracker.isCoolingDown(104));
        assertTrue(tracker.canDiscard(104));
    }

    @Test
    void idleTimeCannotAccumulateUnlimitedBurstAllowance() {
        FlightMovementTracker tracker = new FlightMovementTracker(0);
        finishTick(tracker, 0, DEFAULTS);
        tracker.recordMovement(20.0D, 0.0D, 0.0D);
        assertEquals(SUSPECT, finishTick(tracker, 10_000, DEFAULTS));
    }

    @Test
    void nonFiniteInputDoesNotSilentlyPassComparisons() {
        for (double invalid : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            FlightMovementTracker tracker = new FlightMovementTracker(0);
            tracker.recordMovement(invalid, 0.0D, 0.0D);
            assertEquals(SUSPECT, finishTick(tracker, 0, DEFAULTS));
            tracker.recordMovement(1.0D, 0.0D, 0.0D);
            assertEquals(CLEAN, finishTick(tracker, 1, DEFAULTS));
        }
    }

    @Test
    void tickOverflowDoesNotFreezeCheckingOrCooldown() {
        int start = Integer.MAX_VALUE - 1;
        FlightMovementTracker tracker = new FlightMovementTracker(start);
        for (int offset = 0; offset < 4; offset++) {
            tracker.recordMovement(20.0D, 0.0D, 0.0D);
            assertEquals(offset == 3 ? CORRECT : SUSPECT, finishTick(tracker, start + offset, DEFAULTS));
        }
        tracker.onCorrection(start, 10);
        assertTrue(tracker.isCoolingDown(start + 9));
        assertFalse(tracker.isCoolingDown(start + 10));
    }

    @Test
    void sustainedTenTpsCompensatesRealElapsedTime() {
        FlightMovementTracker tracker = new FlightMovementTracker(0);
        for (int tick = 0; tick < 200; tick++) {
            tracker.recordMovement(3.0D, -7.84D, 0.0D);
            assertEquals(CLEAN, tracker.finishTick(tick, tick * 100_000_000L, DEFAULTS));
        }
    }

    @Test
    void longServerStallDoesNotGrantUnlimitedMovement() {
        FlightMovementTracker tracker = new FlightMovementTracker(0);
        tracker.finishTick(0, 0L, DEFAULTS);
        tracker.recordMovement(20.0D, 0.0D, 0.0D);
        assertEquals(SUSPECT, tracker.finishTick(1, 60_000_000_000L, DEFAULTS));
    }

    @Test
    void highFrequencyChecksDoNotMintExtraTimeAllowance() {
        FlightMovementTracker tracker = new FlightMovementTracker(0);
        tracker.recordMovement(20.0D, 0.0D, 0.0D);
        assertEquals(SUSPECT, tracker.finishTick(0, 0L, DEFAULTS));
        for (int call = 1; call < 100; call++) {
            assertEquals(UNCHANGED, tracker.finishTick(0, call * 100_000L, DEFAULTS));
        }
        tracker.recordMovement(20.0D, 0.0D, 0.0D);
        assertEquals(SUSPECT, tracker.finishTick(1, 50_000_000L, DEFAULTS));
    }

    @Test
    void zeroCooldownAllowsImmediateRestart() {
        FlightMovementTracker tracker = new FlightMovementTracker(0);
        tracker.onCorrection(0, 0);
        assertFalse(tracker.isCoolingDown(0));
    }

    @Test
    void rebaseAndRepeatedTakeoffDoNotRenewSoftGrace() {
        FlightMovementTracker tracker = new FlightMovementTracker(0);
        finishTick(tracker, 0, DEFAULTS);
        tracker.rebase();
        for (int tick = 100; tick < 116; tick++) {
            tracker.markFlying(tick);
            tracker.recordMovement(2.5D, 0.0D, 0.0D);
            var result = finishTick(tracker, tick, DEFAULTS);
            if (tick == 115) {
                assertEquals(CORRECT, result);
            }
        }
    }

    private static FlightMovementTracker.Result finishTick(FlightMovementTracker tracker, int tick,
                                                           FlightAntiCheatSettings settings) {
        return tracker.finishTick(tick, Integer.toUnsignedLong(tick) * 50_000_000L, settings);
    }

    private static FlightAntiCheatSettings withGrace(int ticks) {
        return new FlightAntiCheatSettings(DEFAULTS.enabled(), ticks,
                DEFAULTS.softViolationLimit(), DEFAULTS.hardViolationLimit(), DEFAULTS.correctionCooldownTicks(),
                DEFAULTS.softHorizontalLimit(), DEFAULTS.softVerticalLimit(), DEFAULTS.softDownwardLimit(),
                DEFAULTS.softTotalLimit(), DEFAULTS.hardHorizontalLimit(), DEFAULTS.hardVerticalLimit(),
                DEFAULTS.hardDownwardLimit(), DEFAULTS.hardTotalLimit(),
                DEFAULTS.upwardAssistHorizontalThreshold(), DEFAULTS.upwardAssistMaxBonus());
    }
}

