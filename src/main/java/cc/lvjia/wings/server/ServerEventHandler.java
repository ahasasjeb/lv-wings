package cc.lvjia.wings.server;

import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.server.asm.GetLivingHeadLimitEvent;
import cc.lvjia.wings.server.asm.PlayerFlightCheckEvent;
import cc.lvjia.wings.server.asm.PlayerFlownEvent;
import cc.lvjia.wings.server.command.WingsCommand;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.Flights;
import cc.lvjia.wings.server.item.WingsItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
        Flights.get(event.getEntity()).ifPresent(flight ->
                flight.tick(event.getEntity())
        );
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
