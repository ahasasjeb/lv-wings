package cc.lvjia.wings.server.flight;

import cc.lvjia.wings.WingsAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public final class Flights {
    private Flights() {
    }

    public static boolean has(Player player) {
        return WingsAttachments.hasFlight(player);
    }

    public static Optional<Flight> get(Player player) {
        return Optional.of(WingsAttachments.getFlight(player));
    }

    public static void ifPlayer(Entity entity, BiConsumer<Player, Flight> action) {
        ifPlayer(entity, e -> true, action);
    }

    public static void ifPlayer(Entity entity, Predicate<Player> condition, BiConsumer<Player, Flight> action) {
        if (entity instanceof Player player) {
            get(player).filter(flight -> condition.test(player)).ifPresent(flight -> action.accept(player, flight));
        }
    }

    public static void onPlayerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        if (alive) {
            get(oldPlayer).ifPresent(oldInstance -> get(newPlayer).ifPresent(newInstance -> newInstance.clone(oldInstance)));
        }
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        get(player).ifPresent(flight -> flight.sync(Flight.PlayerSet.ofSelf()));
    }

    public static void onPlayerChangedDimension(ServerPlayer player) {
        get(player).ifPresent(flight -> flight.sync(Flight.PlayerSet.ofSelf()));
    }

    public static void onPlayerLoggedIn(ServerPlayer player) {
        get(player).ifPresent(flight -> flight.sync(Flight.PlayerSet.ofSelf()));
    }

    public static void onPlayerStartTracking(Entity target, ServerPlayer player) {
        ifPlayer(target, (trackedPlayer, flight) -> flight.sync(Flight.PlayerSet.ofPlayer(player)));
    }
}
