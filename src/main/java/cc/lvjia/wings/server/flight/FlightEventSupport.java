package cc.lvjia.wings.server.flight;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class FlightEventSupport {
    private FlightEventSupport() {
    }

    public static void ifPlayer(Entity entity, Function<Player, Flight> flights,
                                BiConsumer<Player, Flight> action) {
        ifPlayer(entity, player -> true, flights, action);
    }

    public static void ifPlayer(Entity entity, Predicate<Player> condition, Function<Player, Flight> flights,
                                BiConsumer<Player, Flight> action) {
        if (entity instanceof Player player && condition.test(player)) {
            action.accept(player, flights.apply(player));
        }
    }

    public static boolean isFlyingPlayer(Entity entity, Function<Player, Flight> flights) {
        return entity instanceof Player player && flights.apply(player).isFlying();
    }

    public static void onPlayerClone(Player oldPlayer, Player newPlayer, boolean copyFlightState,
                                     Function<Player, Flight> flights) {
        if (copyFlightState) {
            flights.apply(newPlayer).clone(flights.apply(oldPlayer));
        }
    }

    public static void syncSelf(Player player, Function<Player, Flight> flights) {
        flights.apply(player).sync(Flight.PlayerSet.ofSelf());
    }

    public static void syncTrackingPlayer(Entity target, ServerPlayer trackingPlayer, Function<Player, Flight> flights) {
        ifPlayer(target, flights, (player, flight) -> flight.sync(Flight.PlayerSet.ofPlayer(trackingPlayer)));
    }
}
