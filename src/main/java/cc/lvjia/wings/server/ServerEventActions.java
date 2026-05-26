package cc.lvjia.wings.server;

import cc.lvjia.wings.server.asm.GetLivingHeadLimitEvent;
import cc.lvjia.wings.server.asm.PlayerFlightCheckEvent;
import cc.lvjia.wings.server.asm.PlayerFlownEvent;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.FlightSpeedAntiCheat;
import cc.lvjia.wings.server.flight.FlightStateReset;
import cc.lvjia.wings.server.flight.Flights;
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
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public final class ServerEventActions {
    private ServerEventActions() {
    }

    public static @NonNull InteractionResult onPlayerEntityInteract(@NonNull Player player,
            @NonNull InteractionHand hand, @NonNull Entity target, @NonNull Supplier<ItemStack> batBloodBottle,
            @Nullable DestroyedItemCallback destroyedItemCallback) {
        ItemStack stack = player.getItemInHand(hand);
        Item glassBottle = Objects.requireNonNull(Items.GLASS_BOTTLE, "glass bottle");
        if (!(target instanceof Bat) || stack.getItem() != glassBottle) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        player.level().playSound(
                player,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.BOTTLE_FILL,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);
        ItemStack destroyed = stack.copy();
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.awardStat(Stats.ITEM_USED.get(glassBottle));
        ItemStack batBlood = Objects.requireNonNull(batBloodBottle.get(), "bat blood bottle");
        if (stack.isEmpty()) {
            if (destroyedItemCallback != null) {
                destroyedItemCallback.onDestroy(player, destroyed, hand);
            }
            player.setItemInHand(hand, batBlood);
        } else if (!player.getInventory().add(batBlood)) {
            player.drop(batBlood, false);
        }
        return InteractionResult.SUCCESS;
    }

    public static void onPlayerTick(@NonNull Player player) {
        Flight flight = Flights.get(player);
        if (FlightStateReset.clearSpectator(player, flight)) {
            return;
        }
        flight.tick(player);
        if (player instanceof ServerPlayer serverPlayer && !serverPlayer.level().isClientSide()) {
            if (flight.isFlying() && player.getAbilities().flying) {
                player.getAbilities().flying = false;
                serverPlayer.onUpdateAbilities();
            }
            FlightSpeedAntiCheat.tick(serverPlayer, flight);
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
        if (Flights.get(event.getEntity()).isFlying()) {
            event.setFlying();
        }
    }

    public static void onPlayerFlown(@NonNull PlayerFlownEvent event) {
        Player player = event.getEntity();
        Flight flight = Flights.get(player);
        if (FlightStateReset.clearSpectator(player, flight)) {
            return;
        }
        flight.onFlown(player, event.getDirection());
        if (player instanceof ServerPlayer serverPlayer && !serverPlayer.level().isClientSide()) {
            FlightSpeedAntiCheat.recordMovement(serverPlayer, flight, event.getDirection());
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

    @FunctionalInterface
    public interface DestroyedItemCallback {
        void onDestroy(@NonNull Player player, @NonNull ItemStack destroyed, @NonNull InteractionHand hand);
    }
}
