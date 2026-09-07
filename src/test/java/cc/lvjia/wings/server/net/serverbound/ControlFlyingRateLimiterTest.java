package cc.lvjia.wings.server.net.serverbound;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ControlFlyingRateLimiterTest {
    private final ControlFlyingRateLimiter limiter = new ControlFlyingRateLimiter();
    private final UUID playerId = UUID.randomUUID();
    private final AtomicInteger corrections = new AtomicInteger();

    @Test
    void floodProducesOneCorrectionAndDoesNotExtendCooldown() {
        assertTrue(limiter.tryAcquire(playerId, 100, corrections::incrementAndGet));
        for (int i = 0; i < 1000; i++) {
            assertFalse(limiter.tryAcquire(playerId, 100, corrections::incrementAndGet));
        }
        assertEquals(0, corrections.get());
        limiter.flushCorrection(playerId);
        limiter.flushCorrection(playerId);
        assertEquals(1, corrections.get());
        assertFalse(limiter.tryAcquire(playerId, 101, corrections::incrementAndGet));
        assertTrue(limiter.tryAcquire(playerId, 102, corrections::incrementAndGet));
        limiter.flushCorrection(playerId);
        assertEquals(1, corrections.get(), "Accepted request supersedes pending correction");
    }

    @Test
    void entityTickResetAndOverflowDoNotLockOutPlayer() {
        assertTrue(limiter.tryAcquire(playerId, 10000, corrections::incrementAndGet));
        assertTrue(limiter.tryAcquire(playerId, 0, corrections::incrementAndGet));
        assertTrue(limiter.tryAcquire(playerId, Integer.MAX_VALUE, corrections::incrementAndGet));
        assertTrue(limiter.tryAcquire(playerId, Integer.MIN_VALUE, corrections::incrementAndGet));
    }

    @Test
    void lifecycleCleanupDropsOldEntityCallbackAndCooldown() {
        assertTrue(limiter.tryAcquire(playerId, 100, corrections::incrementAndGet));
        assertFalse(limiter.tryAcquire(playerId, 100, corrections::incrementAndGet));
        limiter.clear(playerId);
        limiter.flushCorrection(playerId);
        assertEquals(0, corrections.get());
        assertTrue(limiter.tryAcquire(playerId, 100, corrections::incrementAndGet));
    }

    @Test
    void playersHaveIndependentCooldowns() {
        assertTrue(limiter.tryAcquire(playerId, 100, corrections::incrementAndGet));
        UUID otherPlayer = UUID.randomUUID();
        assertTrue(limiter.tryAcquire(otherPlayer, 100, corrections::incrementAndGet));
        assertFalse(limiter.tryAcquire(playerId, 100, corrections::incrementAndGet));
        limiter.flushCorrection(otherPlayer);
        assertEquals(0, corrections.get());
        limiter.flushCorrection(playerId);
        assertEquals(1, corrections.get());
    }
}
