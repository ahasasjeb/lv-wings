package cc.lvjia.wings.client;

import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.client.asm.AnimatePlayerModelEvent;
import cc.lvjia.wings.client.asm.ApplyPlayerRotationsEvent;
import cc.lvjia.wings.client.asm.GetCameraEyeHeightEvent;
import cc.lvjia.wings.client.audio.WingsSound;
import cc.lvjia.wings.client.event.EmptyOffHandPresentEvent;
import cc.lvjia.wings.client.flight.FlightView;
import cc.lvjia.wings.client.flight.FlightViews;
import cc.lvjia.wings.server.flight.Flights;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = WingsMod.ID)
public final class NeoForgeClientEventHandler {
    private NeoForgeClientEventHandler() {
    }

    @SubscribeEvent
    public static void onAnimatePlayerModel(AnimatePlayerModelEvent event) {
        ClientEventHandlerSupport.applyFlightPose(event.getEntity(), event.getModel(), event.getTicksExisted(),
                event.getPitch());
    }

    @SubscribeEvent
    public static void onApplyRotations(ApplyPlayerRotationsEvent event) {
        ClientEventHandlerSupport.applyPlayerRotations(event.getEntity(), event.getMatrixStack(), event.getDelta());
    }

    @SubscribeEvent
    public static void onGetCameraEyeHeight(GetCameraEyeHeightEvent event) {
        ClientEventHandlerSupport.tickCameraEyeHeight(event.getEntity(), event.getValue(), event::setValue);
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        event.setRoll(ClientEventHandlerSupport.computeCameraRoll((float) event.getPartialTick()));
    }

    @SubscribeEvent
    public static void onEmptyOffHandPresentEvent(EmptyOffHandPresentEvent event) {
        if (Flights.get(event.getPlayer()).isFlying()) {
            event.allow();
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        Flights.ifPlayer(event.getEntity(), Player::isLocalPlayer,
                (player, flight) -> Minecraft.getInstance().getSoundManager().play(new WingsSound(player, flight)));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player entity = event.getEntity();
        if (entity instanceof AbstractClientPlayer player) {
            FlightViews.get(player).ifPresent(FlightView::tick);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientEventHandlerSupport.tickClientDimension();
    }

}
