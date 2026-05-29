package cc.lvjia.wings.server.flight;

import cc.lvjia.wings.WingsAttachments;
import cc.lvjia.wings.WingsMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

@EventBusSubscriber(modid = WingsMod.ID)
public final class Flights {
    public static final EntityCapability<Flight, Void> FLIGHT_CAPABILITY =
            EntityCapability.createVoid(WingsMod.locate("flight"), Flight.class);

    private Flights() {
    }

    public static Flight get(Player player) {
        return player.getData(WingsAttachments.FLIGHT.get());
    }

    public static void ifPlayer(Entity entity, BiConsumer<Player, Flight> action) {
        FlightEventSupport.ifPlayer(entity, Flights::get, action);
    }

    public static void ifPlayer(Entity entity, Predicate<Player> condition, BiConsumer<Player, Flight> action) {
        FlightEventSupport.ifPlayer(entity, condition, Flights::get, action);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        FlightEventSupport.onPlayerClone(event.getOriginal(), event.getEntity(), !event.isWasDeath(), Flights::get);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        FlightEventSupport.syncSelf(event.getEntity(), Flights::get);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        FlightEventSupport.syncSelf(event.getEntity(), Flights::get);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        FlightEventSupport.syncSelf(event.getEntity(), Flights::get);
    }

    @SubscribeEvent
    public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        FlightEventSupport.syncTrackingPlayer(event.getTarget(), (ServerPlayer) event.getEntity(), Flights::get);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(FLIGHT_CAPABILITY, EntityType.PLAYER, (player, ctx) ->
                player.getData(WingsAttachments.FLIGHT.get())
        );
    }
}
