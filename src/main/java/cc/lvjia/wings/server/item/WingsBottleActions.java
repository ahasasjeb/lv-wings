package cc.lvjia.wings.server.item;

import cc.lvjia.wings.server.apparatus.FlightApparatus;
import cc.lvjia.wings.server.effect.WingsEffects;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.FlightAnimationState;
import cc.lvjia.wings.server.flight.FlightSpeedAntiCheat;
import cc.lvjia.wings.server.flight.Flights;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public final class WingsBottleActions {
    private WingsBottleActions() {
    }

    public static boolean giveWing(ServerPlayer player, FlightApparatus wings) {
        Flight flight = Flights.get(player);
        boolean changed = false;
        if (flight.getWing() != wings) {
            flight.setWing(wings, Flight.PlayerSet.ofAll());
            changed = true;
        }
        if (WingsEffects.WINGS.isBound()) {
            player.addEffect(
                    new MobEffectInstance(WingsEffects.WINGS, MobEffectInstance.INFINITE_DURATION, 0, true, false));
        }
        return changed;
    }

    public static boolean removeWings(Player player) {
        boolean removed = WingsEffects.WINGS.isBound() && player.removeEffect(WingsEffects.WINGS);
        if (removed) {
            clearFlightState(player);
        }
        return removed;
    }

    public static boolean removeWings(ServerPlayer player, FlightApparatus wings) {
        boolean changed = Flights.get(player).getWing() == wings;
        if (!changed || !WingsEffects.WINGS.isBound() || !player.removeEffect(WingsEffects.WINGS)) {
            return false;
        }
        clearFlightState(player);
        return true;
    }

    private static void clearFlightState(Player player) {
        Flight flight = Flights.get(player);
        Flight.PlayerSet players = player.level().isClientSide() ? Flight.PlayerSet.empty()
                : Flight.PlayerSet.ofAll();
        flight.setIsFlying(false, players);
        flight.setWing(FlightApparatus.NONE, players);
        flight.setTimeFlying(0);
        flight.setAnimationState(FlightAnimationState.IDLE);
        if (player instanceof ServerPlayer serverPlayer) {
            FlightSpeedAntiCheat.clear(serverPlayer);
        }
    }
}
