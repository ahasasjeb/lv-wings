package cc.lvjia.wings.client.net;

import cc.lvjia.wings.server.flight.Flight;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;
import java.util.function.Predicate;

public final class ClientFlightSyncApplier {
    private static final Logger LOGGER = LogManager.getLogger("WingsNetwork");

    private ClientFlightSyncApplier() {
    }

    public static void apply(int playerId, Flight snapshot, Level level, Predicate<Player> hasFlight,
                             Function<Player, Flight> getFlight) {
        if (level == null) {
            LOGGER.warn("Received sync_flight but level is null");
            return;
        }
        var entity = level.getEntity(playerId);
        if (!(entity instanceof Player player)) {
            LOGGER.warn("Received sync_flight for invalid entity id={}", playerId);
            return;
        }
        boolean hadFlight = hasFlight.test(player);
        Flight flight = getFlight.apply(player);
        if (!hadFlight) {
            LOGGER.debug("Creating new flight attachment for player {}", player.getName().getString());
        }
        flight.clone(snapshot);
        LOGGER.debug("Synced flight data for player {} (flying={}, wing={})",
                player.getName().getString(), flight.isFlying(), flight.getWing());
    }
}
