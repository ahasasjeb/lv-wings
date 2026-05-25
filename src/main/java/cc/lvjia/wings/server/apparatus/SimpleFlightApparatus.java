package cc.lvjia.wings.server.apparatus;

import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.item.WingSettings;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public final class SimpleFlightApparatus implements FlightApparatus {
    private final @NonNull WingSettings settings;

    public SimpleFlightApparatus(@NonNull WingSettings settings) {
        this.settings = Objects.requireNonNull(settings);
    }

    @Override
    public void onFlight(@NonNull Player player, @NonNull Vec3 direction) {
        int distance = Math.round((float) direction.length() * 100.0F);
        if (distance > 0) {
            player.causeFoodExhaustion(distance * this.settings.getFlyingExertion());
        }
    }

    @Override
    public void onLanding(@NonNull Player player, @NonNull Vec3 direction) {
        player.causeFoodExhaustion(this.settings.getLandingExertion());
    }

    @Override
    public boolean isUsable(@NonNull Player player) {
        return player.getFoodData().getFoodLevel() >= this.settings.getRequiredFlightSatiation();
    }

    @Override
    public boolean isLandable(@NonNull Player player) {
        return player.getFoodData().getFoodLevel() >= this.settings.getRequiredLandSatiation();
    }

    @Override
    public @NonNull FlightState createState(@NonNull Flight flight) {
        return (player) -> {
        };
    }
}
