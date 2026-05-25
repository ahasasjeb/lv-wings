package cc.lvjia.wings.server;

import cc.lvjia.wings.server.asm.GetLivingHeadLimitEvent;
import cc.lvjia.wings.server.asm.PlayerFlightCheckEvent;
import cc.lvjia.wings.server.asm.PlayerFlownEvent;
import cc.lvjia.wings.server.command.WingsCommand;
import cc.lvjia.wings.server.dreamcatcher.InSomniableEventHandler;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.FlightAnimationState;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

@SuppressWarnings("null")
public final class ServerEventHandler {
    private ServerEventHandler() {
    }

    public static void register() {
        UseEntityCallback.EVENT
                .register((player, level, hand, entity, hitResult) -> onPlayerEntityInteract(player, hand, entity));
        ServerTickEvents.END_SERVER_TICK
                .register(server -> server.getPlayerList().getPlayers().forEach(ServerEventHandler::onPlayerTick));
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
                .register((dispatcher, buildContext, selection) -> WingsCommand.register(dispatcher, buildContext));
        InSomniableEventHandler.register();
    }

    public static @NonNull InteractionResult onPlayerEntityInteract(@NonNull Player player, @NonNull InteractionHand hand,
            @NonNull Entity target) {
        ItemStack stack = player.getItemInHand(hand);
        Item glassBottle = Objects.requireNonNull(Items.GLASS_BOTTLE, "glass bottle");
        if (target instanceof Bat && stack.getItem() == glassBottle) {
            player.level().playSound(
                    player,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BOTTLE_FILL,
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.0F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.awardStat(Stats.ITEM_USED.get(glassBottle));
            ItemStack batBlood = new ItemStack(Objects.requireNonNull(WingsItems.BAT_BLOOD_BOTTLE.get(), "bat blood bottle"));
            if (stack.isEmpty()) {
                player.setItemInHand(hand, batBlood);
            } else if (!player.getInventory().add(batBlood)) {
                player.drop(batBlood, false);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public static void onPlayerTick(@NonNull Player player) {
        Flight flight = Flights.get(player);
        if (!clearSpectatorFlightState(player, flight)) {
            flight.tick(player);
            if (player instanceof ServerPlayer serverPlayer && !serverPlayer.level().isClientSide()) {
                if (flight.isFlying() && player.getAbilities().flying) {
                    player.getAbilities().flying = false;
                    serverPlayer.onUpdateAbilities();
                }
                FlightSpeedAntiCheat.tick(serverPlayer, flight);
            }
        }
    }

    public static void onLivingDeath(@NonNull LivingEntity entity) {
        Flights.ifPlayer(entity, (player, flight) -> {
            flight.setIsFlying(false, Flight.PlayerSet.ofAll());
            FlightSpeedAntiCheat.clear(player);
        });
    }

    public static void onPlayerFlightCheck(@NonNull PlayerFlightCheckEvent event) {
        if (event.getEntity().isSpectator()) {
            return;
        }
        Flight flight = Flights.get(event.getEntity());
        if (flight.isFlying()) {
            event.setFlying();
        }
    }

    public static void onPlayerFlown(@NonNull PlayerFlownEvent event) {
        Player player = event.getEntity();
        Flight flight = Flights.get(player);
        if (!clearSpectatorFlightState(player, flight)) {
            flight.onFlown(player, event.getDirection());
            if (player instanceof ServerPlayer serverPlayer && !serverPlayer.level().isClientSide()) {
                FlightSpeedAntiCheat.recordMovement(serverPlayer, flight, event.getDirection());
            }
        }
    }

    public static void onGetLivingHeadLimit(@NonNull GetLivingHeadLimitEvent event) {
        Flights.ifPlayer(event.getEntity(), (player, flight) -> {
            if (player.isSpectator()) {
                return;
            }
            if (flight.isFlying()) {
                event.setHardLimit(50.0F);
                event.disableSoftLimit();
            }
        });
    }

    private static boolean clearSpectatorFlightState(@NonNull Player player, @NonNull Flight flight) {
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
