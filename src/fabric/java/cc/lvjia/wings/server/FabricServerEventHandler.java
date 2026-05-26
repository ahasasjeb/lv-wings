package cc.lvjia.wings.server;

import cc.lvjia.wings.server.asm.GetLivingHeadLimitEvent;
import cc.lvjia.wings.server.asm.PlayerFlightCheckEvent;
import cc.lvjia.wings.server.asm.PlayerFlownEvent;
import cc.lvjia.wings.server.command.FabricWingsCommand;
import cc.lvjia.wings.server.dreamcatcher.InSomniableEventHandler;
import cc.lvjia.wings.server.flight.FlightSpeedAntiCheat;
import cc.lvjia.wings.server.flight.Flights;
import cc.lvjia.wings.server.item.WingsItems;
import cc.lvjia.wings.server.net.serverbound.MessageControlFlying;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

@SuppressWarnings("null")
public final class FabricServerEventHandler {
    private FabricServerEventHandler() {
    }

    public static void register() {
        UseEntityCallback.EVENT
                .register((player, level, hand, entity, hitResult) -> onPlayerEntityInteract(player, hand, entity));
        ServerTickEvents.END_SERVER_TICK
                .register(server -> server.getPlayerList().getPlayers().forEach(FabricServerEventHandler::onPlayerTick));
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> onLivingDeath(entity));
        ServerPlayerEvents.LEAVE.register(player -> {
            MessageControlFlying.clearRateLimit(player);
            FlightSpeedAntiCheat.clear(player);
        });
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            Flights.onPlayerClone(oldPlayer, newPlayer, alive);
            cc.lvjia.wings.server.dreamcatcher.InSomniableCapability.onPlayerClone(oldPlayer, newPlayer);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> Flights.onPlayerRespawn(newPlayer));
        ServerPlayerEvents.JOIN.register(Flights::onPlayerLoggedIn);
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL
                .register((player, origin, destination) -> Flights.onPlayerChangedDimension(player));
        EntityTrackingEvents.START_TRACKING.register(Flights::onPlayerStartTracking);
        CommandRegistrationCallback.EVENT
                .register((dispatcher, buildContext, selection) -> FabricWingsCommand.register(dispatcher, buildContext));
        InSomniableEventHandler.register();
    }

    public static @NonNull InteractionResult onPlayerEntityInteract(@NonNull Player player, @NonNull InteractionHand hand,
                                                                    @NonNull Entity target) {
        return ServerEventActions.onPlayerEntityInteract(
                player,
                hand,
                target,
                () -> new ItemStack(Objects.requireNonNull(WingsItems.BAT_BLOOD_BOTTLE.get(), "bat blood bottle")),
                null);
    }

    public static void onPlayerTick(@NonNull Player player) {
        ServerEventActions.onPlayerTick(player);
    }

    public static void onLivingDeath(@NonNull LivingEntity entity) {
        ServerEventActions.onLivingDeath(entity);
    }

    public static void onPlayerFlightCheck(@NonNull PlayerFlightCheckEvent event) {
        ServerEventActions.onPlayerFlightCheck(event);
    }

    public static void onPlayerFlown(@NonNull PlayerFlownEvent event) {
        ServerEventActions.onPlayerFlown(event);
    }

    public static void onGetLivingHeadLimit(@NonNull GetLivingHeadLimitEvent event) {
        ServerEventActions.onGetLivingHeadLimit(event);
    }

}
