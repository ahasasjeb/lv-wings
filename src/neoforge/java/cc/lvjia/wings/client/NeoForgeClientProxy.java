package cc.lvjia.wings.client;

import cc.lvjia.wings.NeoForgeProxy;
import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.client.flight.FlightViews;
import cc.lvjia.wings.client.renderer.NeoForgeLayerWings;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.net.serverbound.MessageControlFlying;
import cc.lvjia.wings.server.net.Message;
import cc.lvjia.wings.util.NeoForgeKeyInputListener;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

public final class NeoForgeClientProxy extends NeoForgeProxy {
    private static final Logger LOGGER = LogManager.getLogger("WingsClient");

    private static final KeyMapping.Category WINGS_KEY_CATEGORY = new KeyMapping.Category(WingsMod.locate("wings"));

    public static void registerWingForms(@NonNull EntityModelSet modelSet) {
        WingFormRegistrar.register(modelSet, NeoForgeLayerWings.AVIAN_WINGS, NeoForgeLayerWings.INSECTOID_WINGS);
    }

    @Override
    public void init(IEventBus modBus) {
        super.init(modBus);
        NeoForgeLayerWings.init(modBus);
        NeoForge.EVENT_BUS.register(NeoForgeKeyInputListener.builder()
                .category(WINGS_KEY_CATEGORY)
                .key("key.wings.fly", KeyConflictContext.IN_GAME, KeyModifier.NONE, GLFW.GLFW_KEY_R)
                .onPress(ClientFlightInputActions::toggleLocalFlight)
                .build()
        );

        modBus.addListener(NeoForgeKeyInputListener::registerKeyMappings);
    }

    @Override
    public void addFlightListeners(Player player, Flight flight) {
        super.addFlightListeners(player, flight);
        if (player.isLocalPlayer()) {
            // 本地玩家先更新客户端预测态，再把最终意图发回服务端做校验和纠正
            Flight.Notifier notifier = Flight.Notifier.of(
                    () -> {
                    },
                    p -> {
                    },
                    () -> this.sendToServer(new MessageControlFlying(flight.isFlying()))
            );
            flight.registerSyncListener(players -> players.notify(notifier));
        }
    }

    @Override
    public void sendToServer(Message message) {
        LOGGER.debug("Sending {} to server", message.type().id());
        ClientPacketDistributor.sendToServer(message);
    }

    @Override
    public void invalidateFlightView(Player player) {
        if (player instanceof AbstractClientPlayer clientPlayer) {
            FlightViews.invalidate(clientPlayer);
        }
    }
}
