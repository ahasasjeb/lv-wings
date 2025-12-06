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
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WingsMod.ID)
public final class ServerEventHandler {
    private ServerEventHandler() {
    }

    @SubscribeEvent
    public static void onPlayerEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(hand);
        if (event.getTarget() instanceof Bat && stack.getItem() == Items.GLASS_BOTTLE) {
            player.level().playSound(
                player,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.BOTTLE_FILL,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F
            );
            ItemStack destroyed = stack.copy();
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.awardStat(Stats.ITEM_USED.get(Items.GLASS_BOTTLE));
            ItemStack batBlood = new ItemStack(WingsItems.BAT_BLOOD_BOTTLE.get());
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
        Flights.get(event.getEntity()).filter(Flight::isFlying)
            .ifPresent(flight -> event.setFlying());
    }

    @SubscribeEvent
    public static void onPlayerFlown(PlayerFlownEvent event) {
        Player player = event.getEntity();
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
        MobEffectInstance wings = original.getEffect(WingsEffects.WINGS.get());
        if (wings != null) {
            cloned.addEffect(new MobEffectInstance(wings));
        }
    }

    @SubscribeEvent
    public static void onGetLivingHeadLimit(GetLivingHeadLimitEvent event) {
        Flights.ifPlayer(event.getEntity(), (player, flight) -> {
            if (flight.isFlying()) {
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
