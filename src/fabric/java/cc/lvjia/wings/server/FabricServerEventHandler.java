package cc.lvjia.wings.server;

import cc.lvjia.wings.server.asm.GetLivingHeadLimitEvent;
import cc.lvjia.wings.server.asm.PlayerFlightCheckEvent;
import cc.lvjia.wings.server.asm.PlayerFlownEvent;
import cc.lvjia.wings.server.command.FabricWingsCommand;
import cc.lvjia.wings.server.dreamcatcher.InSomniableEventHandler;
import cc.lvjia.wings.server.flight.FlightSpeedAntiCheat;
import cc.lvjia.wings.server.flight.Flights;
import cc.lvjia.wings.server.item.WingsItems;
import cc.lvjia.wings.server.net.serverbound.MessageControlFlying;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

// Fabric 服务端事件注册中心：将 Fabric API 事件桥接到共享逻辑
@SuppressWarnings("null")
public final class FabricServerEventHandler {
    private FabricServerEventHandler() {
    }

    public static void register() {
        // 右键实体（蝙蝠 → 蝙蝠血瓶）
        UseEntityCallback.EVENT
                .register((player, level, hand, entity, hitResult) -> onPlayerEntityInteract(player, hand, entity));
        // 每 tick 更新所有在线玩家
        ServerTickEvents.END_SERVER_TICK
                .register(server -> server.getPlayerList().getPlayers().forEach(FabricServerEventHandler::onPlayerTick));
        // 死亡时停飞
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> onLivingDeath(entity));
        // 玩家退出时清理限速器和反作弊状态
        ServerPlayerEvents.LEAVE.register(player -> {
            MessageControlFlying.clearRateLimit(player);
            FlightSpeedAntiCheat.clear(player);
        });
        // 玩家克隆（维度切换/重生）时复制飞行和捕梦网状态
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            Flights.onPlayerClone(oldPlayer, newPlayer, alive);
            cc.lvjia.wings.server.dreamcatcher.InSomniableCapability.onPlayerClone(oldPlayer, newPlayer);
        });
        // 重生后同步飞行状态
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> Flights.onPlayerRespawn(newPlayer));
        ServerPlayerEvents.JOIN.register(Flights::onPlayerLoggedIn);
        // 维度切换时同步飞行状态
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL
                .register((player, origin, destination) -> Flights.onPlayerChangedDimension(player));
        // 新玩家开始追踪时推送飞行快照
        EntityTrackingEvents.START_TRACKING.register(Flights::onPlayerStartTracking);
        // 注册 /wings 命令
        CommandRegistrationCallback.EVENT
                .register((dispatcher, buildContext, selection) -> FabricWingsCommand.register(dispatcher, buildContext));
        InSomniableEventHandler.register();
    }

    public static @NonNull InteractionResult onPlayerEntityInteract(@NonNull Player player, @NonNull InteractionHand hand,
                                                                    @NonNull Entity target) {
        return ServerEventActions.onPlayerEntityInteract(
                player,
                hand,
                target,
                () -> new ItemStack(Objects.requireNonNull(WingsItems.BAT_BLOOD_BOTTLE.get(), "bat blood bottle")),
                null);
    }

    public static void onPlayerTick(@NonNull Player player) {
        ServerEventActions.onPlayerTick(player);
    }

    public static void onLivingDeath(@NonNull LivingEntity entity) {
        ServerEventActions.onLivingDeath(entity);
    }

    public static void onPlayerFlightCheck(@NonNull PlayerFlightCheckEvent event) {
        ServerEventActions.onPlayerFlightCheck(event);
    }

    public static void onPlayerFlown(@NonNull PlayerFlownEvent event) {
        ServerEventActions.onPlayerFlown(event);
    }

    public static void onGetLivingHeadLimit(@NonNull GetLivingHeadLimitEvent event) {
        ServerEventActions.onGetLivingHeadLimit(event);
    }

}
