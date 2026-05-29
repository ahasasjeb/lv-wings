package cc.lvjia.wings.server.flight;

import cc.lvjia.wings.WingsAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

@SuppressWarnings("null")
public final class Flights {
    private Flights() {
    }

    public static Flight get(Player player) {
        return WingsAttachments.getFlight(player);
    }

    public static void ifPlayer(Entity entity, BiConsumer<Player, Flight> action) {
        FlightEventSupport.ifPlayer(entity, Flights::get, action);
    }

    public static void ifPlayer(Entity entity, Predicate<Player> condition, BiConsumer<Player, Flight> action) {
        FlightEventSupport.ifPlayer(entity, condition, Flights::get, action);
    }

    public static void onPlayerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        FlightEventSupport.onPlayerClone(oldPlayer, newPlayer, alive, Flights::get);
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        FlightEventSupport.syncSelf(player, Flights::get);
    }

    public static void onPlayerChangedDimension(ServerPlayer player) {
        FlightEventSupport.syncSelf(player, Flights::get);
    }

    public static void onPlayerLoggedIn(ServerPlayer player) {
        FlightEventSupport.syncSelf(player, Flights::get);
    }

    public static void onPlayerStartTracking(Entity target, ServerPlayer player) {
        FlightEventSupport.syncTrackingPlayer(target, player, Flights::get);
    }
}
