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
        ifPlayer(entity, e -> true, action);
    }

    public static void ifPlayer(Entity entity, Predicate<Player> condition, BiConsumer<Player, Flight> action) {
        if (entity instanceof Player player) {
            if (condition.test(player)) {
                action.accept(player, get(player));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            get(event.getEntity()).clone(get(event.getOriginal()));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        get(event.getEntity()).sync(Flight.PlayerSet.ofSelf());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        get(event.getEntity()).sync(Flight.PlayerSet.ofSelf());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        get(event.getEntity()).sync(Flight.PlayerSet.ofSelf());
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
