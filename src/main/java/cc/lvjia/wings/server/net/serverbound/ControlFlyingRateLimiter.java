package cc.lvjia.wings.server.net.serverbound;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 仅在服务端主线程使用；合并超频请求的纠正回包，避免逐包回复。 */
final class ControlFlyingRateLimiter {
    private static final int MIN_INTERVAL_TICKS = 2;
    private final Map<UUID, Integer> lastControlTicks = new HashMap<>();
    private final Map<UUID, Runnable> pendingCorrections = new HashMap<>();

    boolean tryAcquire(UUID playerId, int tick, Runnable correction) {
        Integer previous = lastControlTicks.get(playerId);
        // 实体重建或计数溢出时允许重新开始，不等待旧实体的计数。
        long elapsed = previous == null ? MIN_INTERVAL_TICKS : (long) tick - previous;
        if (elapsed >= 0 && elapsed < MIN_INTERVAL_TICKS) {
            pendingCorrections.put(playerId, correction);
            return false;
        }
        lastControlTicks.put(playerId, tick);
        pendingCorrections.remove(playerId);
        return true;
    }

    void flushCorrection(UUID playerId) {
        Runnable correction = pendingCorrections.remove(playerId);
        if (correction != null) {
            correction.run();
        }
    }

    void clear(UUID playerId) {
        lastControlTicks.remove(playerId);
        pendingCorrections.remove(playerId);
    }
}
