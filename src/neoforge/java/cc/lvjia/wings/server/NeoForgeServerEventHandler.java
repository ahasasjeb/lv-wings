package cc.lvjia.wings.server;

import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.server.asm.GetLivingHeadLimitEvent;
import cc.lvjia.wings.server.asm.PlayerFlightCheckEvent;
import cc.lvjia.wings.server.asm.PlayerFlownEvent;
import cc.lvjia.wings.server.command.NeoForgeWingsCommand;
import cc.lvjia.wings.server.flight.FlightSpeedAntiCheat;
import cc.lvjia.wings.server.flight.Flights;
import cc.lvjia.wings.server.item.WingsItems;
import cc.lvjia.wings.server.net.serverbound.MessageControlFlying;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
public final class NeoForgeServerEventHandler {
    private NeoForgeServerEventHandler() {
    }

    @SubscribeEvent
    public static void onPlayerEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        InteractionResult result = ServerEventActions.onPlayerEntityInteract(
                player,
                hand,
                event.getTarget(),
                () -> new ItemStack(WingsItems.BAT_BLOOD_BOTTLE.get()),
                EventHooks::onPlayerDestroyItem);
        if (result != InteractionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
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
        ServerEventActions.onPlayerTick(event.getEntity());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        ServerEventActions.onLivingDeath(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        MessageControlFlying.clearRateLimit(event.getEntity());
        FlightSpeedAntiCheat.clear(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerFlightCheck(PlayerFlightCheckEvent event) {
        ServerEventActions.onPlayerFlightCheck(event);
    }

    @SubscribeEvent
    public static void onPlayerFlown(PlayerFlownEvent event) {
        ServerEventActions.onPlayerFlown(event);
    }

    @SubscribeEvent
    public static void onGetLivingHeadLimit(GetLivingHeadLimitEvent event) {
        ServerEventActions.onGetLivingHeadLimit(event);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        NeoForgeWingsCommand.register(event.getDispatcher());
    }

}
