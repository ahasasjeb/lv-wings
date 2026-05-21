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
        ifPlayer(entity, e -> true, action);
    }

    public static void ifPlayer(Entity entity, Predicate<Player> condition, BiConsumer<Player, Flight> action) {
        if (entity instanceof Player player) {
            if (condition.test(player)) {
                action.accept(player, get(player));
            }
        }
    }

    public static void onPlayerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        if (alive) {
            get(newPlayer).clone(get(oldPlayer));
        }
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        get(player).sync(Flight.PlayerSet.ofSelf());
    }

    public static void onPlayerChangedDimension(ServerPlayer player) {
        get(player).sync(Flight.PlayerSet.ofSelf());
    }

    public static void onPlayerLoggedIn(ServerPlayer player) {
        get(player).sync(Flight.PlayerSet.ofSelf());
    }

    public static void onPlayerStartTracking(Entity target, ServerPlayer player) {
        if (target instanceof Player trackedPlayer) {
            get(trackedPlayer).sync(Flight.PlayerSet.ofPlayer(player));
        }
    }
}
