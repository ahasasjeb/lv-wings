package com.toni.wings.server;

import com.toni.wings.WingsMod;
import com.toni.wings.server.asm.GetLivingHeadLimitEvent;
import com.toni.wings.server.asm.PlayerFlightCheckEvent;
import com.toni.wings.server.asm.PlayerFlownEvent;
import com.toni.wings.server.command.WingsCommand;
import com.toni.wings.server.effect.WingsEffects;
import com.toni.wings.server.flight.Flight;
import com.toni.wings.server.flight.Flights;
import com.toni.wings.server.item.WingsItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = WingsMod.ID)
public final class ServerEventHandler {
    private ServerEventHandler() {
    }

    @SubscribeEvent
    public static void onPlayerEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        InteractionHand hand = Objects.requireNonNull(event.getHand(), "Interaction hand cannot be null");
        ItemStack stack = player.getItemInHand(hand);
        if (event.getTarget() instanceof Bat && stack.getItem() == Items.GLASS_BOTTLE) {
            player.level().playSound(
                player,
                player.getX(), player.getY(), player.getZ(),
                Objects.requireNonNull(SoundEvents.BOTTLE_FILL, "Bottle fill sound cannot be null"),
                SoundSource.NEUTRAL,
                1.0F,
                1.0F
            );
            ItemStack destroyed = stack.copy();
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.awardStat(Objects.requireNonNull(
                Stats.ITEM_USED.get(Objects.requireNonNull(Items.GLASS_BOTTLE, "Glass bottle item cannot be null")),
                "Glass bottle use stat cannot be null"
            ));
            ItemStack batBlood = new ItemStack(Objects.requireNonNull(WingsItems.BAT_BLOOD_BOTTLE.get(), "Bat blood bottle item cannot be null"));
            if (stack.isEmpty()) {
                ForgeEventFactory.onPlayerDestroyItem(player, destroyed, hand);
                player.setItemInHand(hand, batBlood);
            } else if (!player.getInventory().add(batBlood)) {
                player.drop(batBlood, false);
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        if (event.isMounting()) {
            Flights.ifPlayer(event.getEntityMounting(), (player, flight) -> {
                if (flight.isFlying()) {
                    event.setCanceled(true);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Flights.get(event.player).ifPresent(flight ->
                flight.tick(event.player)
            );
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Flights.ifPlayer(event.getEntity(), (player, flight) ->
            flight.setIsFlying(false, Flight.PlayerSet.ofAll())
        );
    }

    @SubscribeEvent
    public static void onPlayerFlightCheck(PlayerFlightCheckEvent event) {
        Player player = event.getEntity();
        Flights.get(player).filter(flight -> flight.isFlying() && !player.isSpectator() && !player.getAbilities().flying)
            .ifPresent(flight -> event.setFlying());
    }

    @SubscribeEvent
    public static void onPlayerFlown(PlayerFlownEvent event) {
        Player player = event.getEntity();
        if (player.isSpectator() || player.getAbilities().flying) {
            return;
        }
        Flights.get(player).ifPresent(flight -> {
            flight.onFlown(player, event.getDirection());
        });
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            return;
        }
        Player cloned = event.getEntity();
        if (cloned.level().isClientSide) {
            return;
        }
        Player original = event.getOriginal();
        MobEffectInstance wings = original.getEffect(Objects.requireNonNull(WingsEffects.WINGS.get(), "Wings effect cannot be null"));
        if (wings != null) {
            cloned.addEffect(new MobEffectInstance(Objects.requireNonNull(wings, "Wings effect instance cannot be null")));
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (event.getNewGameMode() == GameType.SPECTATOR) {
            Flights.get(event.getEntity()).filter(Flight::isFlying)
                .ifPresent(flight -> flight.setIsFlying(false, Flight.PlayerSet.ofAll()));
        }
    }

    @SubscribeEvent
    public static void onGetLivingHeadLimit(GetLivingHeadLimitEvent event) {
        Flights.ifPlayer(event.getEntity(), (player, flight) -> {
            if (flight.isFlying() && !player.isSpectator() && !player.getAbilities().flying) {
                event.setHardLimit(50.0F);
                event.disableSoftLimit();
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        WingsCommand.register(event.getDispatcher());
    }
}
