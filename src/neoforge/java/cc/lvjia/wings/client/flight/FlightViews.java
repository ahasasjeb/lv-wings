package cc.lvjia.wings.client.flight;

import cc.lvjia.wings.server.flight.Flights;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public final class FlightViews {

    private static final Map<AbstractClientPlayer, FlightView> VIEWS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private FlightViews() {
    }

    public static boolean has(LivingEntity entity) {
        return get(entity).isPresent();
    }

    public static Optional<FlightView> get(LivingEntity entity) {
        if (entity instanceof AbstractClientPlayer player) {
            return Flights.get(player)
                    .map(flight -> VIEWS.computeIfAbsent(player, ignored -> new FlightViewDefault(player, flight)));
        }
        return Optional.empty();
    }

    public static void invalidate(AbstractClientPlayer player) {
        VIEWS.remove(player);
    }
}
