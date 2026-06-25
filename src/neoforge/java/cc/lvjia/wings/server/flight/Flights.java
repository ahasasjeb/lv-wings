package cc.lvjia.wings.server.flight;

import cc.lvjia.wings.WingsAttachments;
import cc.lvjia.wings.WingsMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

// NeoForge 版 Flight 访问入口：通过 Data Attachment + Capability 获取玩家飞行状态
@EventBusSubscriber(modid = WingsMod.ID)
public final class Flights {
    // NeoForge EntityCapability，允许其他模组通过 capability 查询飞行状态
    public static final EntityCapability<Flight, Void> FLIGHT_CAPABILITY =
            EntityCapability.createVoid(WingsMod.locate("flight"), Flight.class);

    private Flights() {
    }

    // 通过 NeoForge Data Attachment 获取玩家的 Flight 实例
    public static Flight get(Player player) {
        return player.getData(WingsAttachments.FLIGHT.get());
    }

    // 类型安全的 Entity→Player 转换 + Flight 获取
    public static void ifPlayer(Entity entity, BiConsumer<Player, Flight> action) {
        FlightEventSupport.ifPlayer(entity, Flights::get, action);
    }

    public static void ifPlayer(Entity entity, Predicate<Player> condition, BiConsumer<Player, Flight> action) {
        FlightEventSupport.ifPlayer(entity, condition, Flights::get, action);
    }

    // 玩家克隆时复制飞行状态
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        FlightEventSupport.onPlayerClone(event.getOriginal(), event.getEntity(), !event.isWasDeath(), Flights::get);
    }

    // 重生/换维/登录时向自己同步飞行状态
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        FlightEventSupport.syncSelf(event.getEntity(), Flights::get);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        FlightEventSupport.syncSelf(event.getEntity(), Flights::get);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        FlightEventSupport.syncSelf(event.getEntity(), Flights::get);
    }

    // 新追踪者加入时推送飞行快照
    @SubscribeEvent
    public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        FlightEventSupport.syncTrackingPlayer(event.getTarget(), (ServerPlayer) event.getEntity(), Flights::get);
    }

    // 注册 EntityCapability，使飞行状态可通过 capability 系统访问
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(FLIGHT_CAPABILITY, EntityTypes.PLAYER, (player, ctx) ->
                player.getData(WingsAttachments.FLIGHT.get())
        );
    }
}
