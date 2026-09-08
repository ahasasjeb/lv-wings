package cc.lvjia.wings.server.flight;

import cc.lvjia.wings.WingsAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

// Fabric 版 Flight 访问入口：通过 Data Attachment 获取玩家飞行状态
@SuppressWarnings("null")
public final class Flights {
    private Flights() {
    }

    // 通过 Fabric Attachment API 获取玩家的 Flight 实例
    public static Flight get(Player player) {
        return WingsAttachments.getFlight(player);
    }

    // 类型安全的 Entity→Player 转换 + Flight 获取
    public static void ifPlayer(Entity entity, BiConsumer<Player, Flight> action) {
        FlightEventSupport.ifPlayer(entity, Flights::get, action);
    }

    public static void ifPlayer(Entity entity, Predicate<@NonNull Player> condition, BiConsumer<Player, Flight> action) {
        FlightEventSupport.ifPlayer(entity, condition, Flights::get, action);
    }

    // 事件桥接：玩家克隆时复制飞行状态
    public static void onPlayerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        FlightEventSupport.onPlayerClone(oldPlayer, newPlayer, alive, Flights::get);
    }

    // 事件桥接：重生/换维/登录时向自己同步飞行状态
    public static void onPlayerRespawn(ServerPlayer player) {
        FlightEventSupport.syncSelf(player, Flights::get);
    }

    public static void onPlayerChangedDimension(ServerPlayer player) {
        FlightEventSupport.syncSelf(player, Flights::get);
    }

    public static void onPlayerLoggedIn(ServerPlayer player) {
        FlightEventSupport.syncSelf(player, Flights::get);
    }

    // 事件桥接：新追踪者加入时推送飞行快照
    public static void onPlayerStartTracking(Entity target, ServerPlayer player) {
        FlightEventSupport.syncTrackingPlayer(target, player, Flights::get);
    }
}
