package cc.lvjia.wings.server;

import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.server.asm.GetLivingHeadLimitEvent;
import cc.lvjia.wings.server.asm.PlayerFlightCheckEvent;
import cc.lvjia.wings.server.asm.PlayerFlownEvent;
import cc.lvjia.wings.server.command.WingsCommand;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.FlightAnimationState;
import cc.lvjia.wings.server.flight.FlightSpeedAntiCheat;
import cc.lvjia.wings.server.flight.Flights;
import cc.lvjia.wings.server.item.WingsItems;
import cc.lvjia.wings.server.net.serverbound.MessageControlFlying;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = WingsMod.ID)
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
                EventHooks.onPlayerDestroyItem(player, destroyed, hand);
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
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Flights.get(player).ifPresent(flight -> {
            if (clearSpectatorFlightState(player, flight)) {
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
        });
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Flights.ifPlayer(event.getEntity(), (player, flight) -> {
            flight.setIsFlying(false, Flight.PlayerSet.ofAll());
            FlightSpeedAntiCheat.clear(player);
        });
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        MessageControlFlying.clearRateLimit(event.getEntity());
        FlightSpeedAntiCheat.clear(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerFlightCheck(PlayerFlightCheckEvent event) {
        if (event.getEntity().isSpectator()) {
            return;
        }
        Flights.get(event.getEntity()).filter(Flight::isFlying)
                .ifPresent(flight -> event.setFlying());
    }

    @SubscribeEvent
    public static void onPlayerFlown(PlayerFlownEvent event) {
        Player player = event.getEntity();
        Flights.get(player).ifPresent(flight -> {
            if (clearSpectatorFlightState(player, flight)) {
                return;
            }
            flight.onFlown(player, event.getDirection());
            if (player instanceof ServerPlayer serverPlayer && !serverPlayer.level().isClientSide()) {
                FlightSpeedAntiCheat.recordMovement(serverPlayer, flight, event.getDirection());
            }
        });
    }

    @SubscribeEvent
    public static void onGetLivingHeadLimit(GetLivingHeadLimitEvent event) {
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

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        WingsCommand.register(event.getDispatcher());
    }

    private static boolean clearSpectatorFlightState(Player player, Flight flight) {
        if (!player.isSpectator()) {
            return false;
        }
        boolean wasFlying = flight.isFlying();
        boolean changed = false;
        if (wasFlying) {
            flight.setIsFlying(false, Flight.PlayerSet.ofAll());
            changed = true;
        }
        if (flight.getTimeFlying() != 0) {
            flight.setTimeFlying(0);
            changed = true;
        }
        if (flight.getAnimationState() != FlightAnimationState.IDLE) {
            flight.setAnimationState(FlightAnimationState.IDLE);
            changed = true;
        }
        if (changed && !wasFlying) {
            flight.sync(Flight.PlayerSet.ofAll());
        }
        FlightSpeedAntiCheat.clear(player);
        return true;
    }
}
