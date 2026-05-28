package cc.lvjia.wings.client;

import cc.lvjia.wings.client.asm.AnimatePlayerModelEvent;
import cc.lvjia.wings.client.asm.ApplyPlayerRotationsEvent;
import cc.lvjia.wings.client.asm.GetCameraEyeHeightEvent;
import cc.lvjia.wings.client.audio.WingsSound;
import cc.lvjia.wings.client.event.EmptyOffHandPresentEvent;
import cc.lvjia.wings.client.flight.FlightView;
import cc.lvjia.wings.client.flight.FlightViews;
import cc.lvjia.wings.server.flight.Flights;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class FabricClientEventHandler {
    private FabricClientEventHandler() {
    }

    public static void register() {
        ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> onEntityJoinWorld(entity));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!client.isPaused() && client.level != null) {
                for (Player player : client.level.players()) {
                    if (player instanceof AbstractClientPlayer clientPlayer) {
                        Flights.get(clientPlayer).tick(clientPlayer);
                        FlightViews.get(clientPlayer).ifPresent(FlightView::tick);
                    }
                }
            }
            onClientTick();
        });
    }

    public static void onAnimatePlayerModel(AnimatePlayerModelEvent event) {
        ClientEventHandlerSupport.applyFlightPose(event.getEntity(), event.getModel(), event.getTicksExisted(),
                event.getPitch());
    }

    public static void onApplyRotations(ApplyPlayerRotationsEvent event) {
        ClientEventHandlerSupport.applyPlayerRotations(event.getEntity(), event.getMatrixStack(), event.getDelta());
    }

    public static void onGetCameraEyeHeight(GetCameraEyeHeightEvent event) {
        ClientEventHandlerSupport.tickCameraEyeHeight(event.getEntity(), event.getValue(), event::setValue);
    }

    public static float computeCameraRoll(float delta) {
        return ClientEventHandlerSupport.computeCameraRoll(delta);
    }

    public static void onEmptyOffHandPresentEvent(EmptyOffHandPresentEvent event) {
        if (Flights.get(event.getPlayer()).isFlying()) {
            event.allow();
        }
    }

    public static void onEntityJoinWorld(Entity entity) {
        Flights.ifPlayer(entity, Player::isLocalPlayer,
                (player, flight) -> Minecraft.getInstance().getSoundManager().play(new WingsSound(player, flight)));
    }

    public static void onClientTick() {
        ClientEventHandlerSupport.tickClientDimension();
    }

}
