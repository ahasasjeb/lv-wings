package cc.lvjia.wings.client;

import cc.lvjia.wings.FabricProxy;
import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.client.flight.FlightViews;
import cc.lvjia.wings.client.net.ClientNetwork;
import cc.lvjia.wings.client.renderer.FabricLayerWings;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.net.Message;
import cc.lvjia.wings.server.net.serverbound.MessageControlFlying;
import cc.lvjia.wings.util.FabricKeyInputListener;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

public final class FabricClientProxy extends FabricProxy {
    private static final Logger LOGGER = LogManager.getLogger("WingsClient");

    private static final KeyMapping.Category WINGS_KEY_CATEGORY = new KeyMapping.Category(WingsMod.locate("wings"));

    public static void registerWingForms(@NonNull EntityModelSet modelSet) {
        WingFormRegistrar.register(modelSet, FabricLayerWings.AVIAN_WINGS, FabricLayerWings.INSECTOID_WINGS);
    }

    public void initClient() {
        ClientNetwork.register();
        FabricLayerWings.init();
        FabricClientEventHandler.register();
        FabricKeyInputListener.builder()
                .category(WINGS_KEY_CATEGORY)
                .key("key.wings.fly", GLFW.GLFW_KEY_R)
                .onPress(ClientFlightInputActions::toggleLocalFlight)
                .build()
                .register();
    }

    @Override
    public void addFlightListeners(Player player, Flight flight) {
        super.addFlightListeners(player, flight);
        if (player.isLocalPlayer()) {
            Flight.Notifier notifier = Flight.Notifier.of(
                    () -> {
                    },
                    p -> {
                    },
                    () -> this.sendToServer(new MessageControlFlying(flight.isFlying())));
            flight.registerSyncListener(players -> players.notify(notifier));
        }
    }

    @Override
    public void sendToServer(Message message) {
        LOGGER.debug("Sending {} to server", message.type().id());
        ClientPlayNetworking.send(message);
    }

    @Override
    public void invalidateFlightView(Player player) {
        if (player instanceof AbstractClientPlayer clientPlayer) {
            FlightViews.invalidate(clientPlayer);
        }
    }
}
