package cc.lvjia.wings.server.net.serverbound;

import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.FlightStateReset;
import cc.lvjia.wings.server.flight.FlightSpeedAntiCheat;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ControlFlyingMessageHandler {
    private static final Logger LOGGER = LogManager.getLogger("WingsNetwork");
    private static final ControlFlyingRateLimiter RATE_LIMITER = new ControlFlyingRateLimiter();

    private ControlFlyingMessageHandler() {
    }

    public static void handle(Player player, boolean isFlying, FlightGetter flightGetter, FlightSync sync) {
        if (!RATE_LIMITER.tryAcquire(player.getUUID(), player.tickCount,
                () -> sync.send(player, flightGetter.get(player)))) {
            return;
        }

        Flight flight = flightGetter.get(player);
        boolean wasFlying = flight.isFlying();
        if (FlightStateReset.clearSpectator(player, flight)) {
            if (wasFlying) {
                LOGGER.debug("Player {} is spectator, forcing wings flight off", player.getName().getString());
            }
            sync.send(player, flight);
            return;
        }
        if (isFlying && (!player.isAlive() || !flight.canFly(player)
                || !FlightSpeedAntiCheat.canStartFlight(player))) {
            LOGGER.debug("Player {} cannot start wings flight or is in correction cooldown", player.getName().getString());
            sync.send(player, flight);
            return;
        }

        LOGGER.debug("Player {} {} flying", player.getName().getString(), isFlying ? "started" : "stopped");
        flight.setIsFlying(isFlying, Flight.PlayerSet.ofOthers());
        sync.send(player, flight);
    }

    public static void clearRateLimit(Player player) {
        RATE_LIMITER.clear(player.getUUID());
    }

    public static void flushCorrection(Player player) {
        RATE_LIMITER.flushCorrection(player.getUUID());
    }

    @FunctionalInterface
    public interface FlightGetter {
        Flight get(Player player);
    }

    @FunctionalInterface
    public interface FlightSync {
        void send(Player player, Flight flight);
    }
}
