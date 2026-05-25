package cc.lvjia.wings.server.flight;

import net.minecraft.world.entity.player.Player;

public final class FlightStateReset {
    private FlightStateReset() {
    }

    public static boolean clearSpectator(Player player, Flight flight) {
        if (!player.isSpectator()) {
            return false;
        }
        boolean wasFlying = flight.isFlying();
        boolean changed = false;
        if (flight.getTimeFlying() != 0) {
            flight.setTimeFlying(0);
            changed = true;
        }
        if (flight.getAnimationState() != FlightAnimationState.IDLE) {
            flight.setAnimationState(FlightAnimationState.IDLE);
            changed = true;
        }
        if (wasFlying) {
            flight.setIsFlying(false, Flight.PlayerSet.ofAll());
        } else if (changed) {
            flight.sync(Flight.PlayerSet.ofAll());
        }
        FlightSpeedAntiCheat.clear(player);
        return true;
    }
}
