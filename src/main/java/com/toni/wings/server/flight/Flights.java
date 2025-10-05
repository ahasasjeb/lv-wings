package com.toni.wings.server.flight;

import com.toni.wings.WingsAttachments;
import com.toni.wings.WingsMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@EventBusSubscriber(modid = WingsMod.ID)
public final class Flights {
    private Flights() {
    }

    public static final EntityCapability<Flight, Void> FLIGHT_CAPABILITY =
        EntityCapability.createVoid(WingsMod.locate("flight"), Flight.class);

    public static boolean has(Player player) {
        return resolve(player) != null;
    }

    public static Optional<Flight> get(Player player) {
        return Optional.ofNullable(resolve(player));
    }

    private static Flight resolve(Player player) {
        Flight flight = player.getCapability(FLIGHT_CAPABILITY);
        if (flight == null && player.hasData(WingsAttachments.FLIGHT.get())) {
            flight = player.getData(WingsAttachments.FLIGHT.get());
        }
        return flight;
    }

    public static void ifPlayer(Entity entity, BiConsumer<Player, Flight> action) {
        ifPlayer(entity, e -> true, action);
    }

    public static void ifPlayer(Entity entity, Predicate<Player> condition, BiConsumer<Player, Flight> action) {
        if (entity instanceof Player player) {
            get(player).filter(flight -> condition.test(player)).ifPresent(flight -> action.accept(player, flight));
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            get(event.getOriginal()).ifPresent(oldInstance ->
                get(event.getEntity()).ifPresent(newInstance -> newInstance.clone(oldInstance))
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        get(event.getEntity()).ifPresent(flight -> flight.sync(Flight.PlayerSet.ofSelf()));
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        get(event.getEntity()).ifPresent(flight -> flight.sync(Flight.PlayerSet.ofSelf()));
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        get(event.getEntity()).ifPresent(flight -> flight.sync(Flight.PlayerSet.ofSelf()));
    }

    @SubscribeEvent
    public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        ifPlayer(event.getTarget(), (player, flight) ->
            flight.sync(Flight.PlayerSet.ofPlayer((ServerPlayer) event.getEntity()))
        );
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(FLIGHT_CAPABILITY, EntityType.PLAYER, (player, ctx) ->
            player.getData(WingsAttachments.FLIGHT.get())
        );
    }
}
