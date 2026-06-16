package cc.lvjia.wings.server.flight;

import cc.lvjia.wings.WingsCore;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;

public final class FlightAbilitySupport {
    private static final AttributeModifier WING_FLIGHT_MODIFIER = new AttributeModifier(
            WingsCore.locate("wing_flight"), 1.0, AttributeModifier.Operation.ADD_VALUE);

    private FlightAbilitySupport() {
    }

    public static void applyFlyingState(Player player, boolean isFlying) {
        AttributeInstance attribute = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (attribute != null) {
            if (isFlying) {
                attribute.addOrUpdateTransientModifier(WING_FLIGHT_MODIFIER);
            } else {
                attribute.removeModifier(WING_FLIGHT_MODIFIER.id());
            }
        }

        boolean hasVanillaFlight = player.getAbilities().instabuild || player.isSpectator();
        if (isFlying || !hasVanillaFlight) {
            player.getAbilities().flying = false;
        }
    }
}