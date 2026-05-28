package cc.lvjia.wings.server.net.serverbound;

import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.FlightStateReset;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ControlFlyingMessageHandler {
    private static final Logger LOGGER = LogManager.getLogger("WingsNetwork");
    private static final int MIN_CONTROL_INTERVAL_TICKS = 2;
    private static final Map<UUID, Integer> LAST_CONTROL_TICKS = new ConcurrentHashMap<>();

    private ControlFlyingMessageHandler() {
    }

    public static void handle(Player player, boolean isFlying, FlightGetter flightGetter, FlightSync sync) {
        Integer lastControlTick = LAST_CONTROL_TICKS.get(player.getUUID());
        if (lastControlTick != null && player.tickCount - lastControlTick < MIN_CONTROL_INTERVAL_TICKS) {
            return;
        }
        LAST_CONTROL_TICKS.put(player.getUUID(), player.tickCount);

        Flight flight = flightGetter.get(player);
        boolean wasFlying = flight.isFlying();
        if (FlightStateReset.clearSpectator(player, flight)) {
            if (wasFlying) {
                LOGGER.debug("Player {} is spectator, forcing wings flight off", player.getName().getString());
            }
            sync.send(player, flight);
            return;
        }
        if (!flight.canFly(player)) {
            LOGGER.debug("Player {} failed canFly check, ignoring control_flying", player.getName().getString());
            sync.send(player, flight);
            return;
        }

        LOGGER.debug("Player {} {} flying", player.getName().getString(), isFlying ? "started" : "stopped");
        flight.setIsFlying(isFlying, Flight.PlayerSet.ofOthers());
        sync.send(player, flight);
    }

    public static void clearRateLimit(Player player) {
        LAST_CONTROL_TICKS.remove(player.getUUID());
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
