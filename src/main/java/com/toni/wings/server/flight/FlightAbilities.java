package com.toni.wings.server.flight;

import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;

public final class FlightAbilities {
    private FlightAbilities() {
    }

    public static boolean canUseModFlight(Player player) {
        return !player.isSpectator();
    }

    public static void stopVanillaFlying(Player player) {
        Abilities abilities = player.getAbilities();
        if (abilities.flying && !player.isSpectator()) {
            abilities.flying = false;
            player.onUpdateAbilities();
        }
    }

    public static void updateForModFlight(Player player, boolean modFlying) {
        Abilities abilities = player.getAbilities();
        boolean vanillaFlight = hasVanillaFlight(player);
        boolean mayfly = modFlying || vanillaFlight;
        boolean flying = abilities.flying;

        if (modFlying || !mayfly) {
            flying = false;
        } else if (player.isSpectator()) {
            flying = true;
        }

        if (abilities.mayfly != mayfly || abilities.flying != flying) {
            boolean syncAbilities = abilities.flying != flying || vanillaFlight && abilities.mayfly != mayfly;
            abilities.mayfly = mayfly;
            abilities.flying = flying;
            if (syncAbilities) {
                player.onUpdateAbilities();
            }
        }
    }

    private static boolean hasVanillaFlight(Player player) {
        return player.isCreative() || player.isSpectator();
    }
}
