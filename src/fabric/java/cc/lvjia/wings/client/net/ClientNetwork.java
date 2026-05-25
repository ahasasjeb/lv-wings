package cc.lvjia.wings.client.net;

import cc.lvjia.wings.WingsAttachments;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.net.clientbound.MessageSyncFlight;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Fabric 客户端网络接收注册。
 */
@SuppressWarnings("null")
public final class ClientNetwork {
    private static final Logger LOGGER = LogManager.getLogger("WingsNetwork");

    private ClientNetwork() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(MessageSyncFlight.TYPE, ClientNetwork::handleSyncFlight);
    }

    private static void handleSyncFlight(MessageSyncFlight message, ClientPlayNetworking.Context context) {
        // 网络线程回调：通过 client executor 切到主线程安全更新世界/实体数据。
        context.client().execute(() -> {
            var level = Minecraft.getInstance().level;
            if (level == null) {
                LOGGER.warn("Received sync_flight but level is null");
                return;
            }
            var entity = level.getEntity(message.playerId());
            if (!(entity instanceof Player player)) {
                LOGGER.warn("Received sync_flight for invalid entity id={}", message.playerId());
                return;
            }
            boolean hadFlight = WingsAttachments.hasFlight(player);
            Flight flight = WingsAttachments.getFlight(player);
            if (!hadFlight) {
                LOGGER.debug("Creating new flight attachment for player {}", player.getName().getString());
            }
            // 客户端直接用整份快照覆盖本地状态，不做增量合并，避免残留旧字段。
            flight.clone(message.flight());
            LOGGER.debug("Synced flight data for player {} (flying={}, wing={})",
                    player.getName().getString(), flight.isFlying(), flight.getWing());
        });
    }
}
