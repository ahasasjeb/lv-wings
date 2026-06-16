package cc.lvjia.wings.server.flight;

import net.minecraft.world.entity.player.Player;

public final class FlightAbilitySupport {
    private FlightAbilitySupport() {
    }

    public static void applyFlyingState(Player player, boolean isFlying) {
        boolean hasVanillaFlight = player.getAbilities().instabuild || player.isSpectator();
        player.getAbilities().mayfly = isFlying || hasVanillaFlight;
        if (isFlying || !hasVanillaFlight) {
            player.getAbilities().flying = false;
        }
    }
}