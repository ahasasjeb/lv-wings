package cc.lvjia.wings.server.flight;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

// 飞行事件的共享辅助方法：类型安全的 Entity→Player 转换 + Flight 获取
public final class FlightEventSupport {
    private FlightEventSupport() {
    }

    // 如果 entity 是 Player，取出 Flight 后执行 action
    public static void ifPlayer(Entity entity, Function<Player, Flight> flights,
                                BiConsumer<Player, Flight> action) {
        ifPlayer(entity, player -> true, flights, action);
    }

    // 带条件的 ifPlayer，只有满足 condition 时才执行
    public static void ifPlayer(Entity entity, Predicate<Player> condition, Function<Player, Flight> flights,
                                BiConsumer<Player, Flight> action) {
        if (entity instanceof Player player && condition.test(player)) {
            action.accept(player, flights.apply(player));
        }
    }

    // 判断 entity 是否是正在飞行的玩家
    public static boolean isFlyingPlayer(Entity entity, Function<Player, Flight> flights) {
        return entity instanceof Player player && flights.apply(player).isFlying();
    }

    // 玩家克隆（如维度切换）时复制飞行状态
    public static void onPlayerClone(Player oldPlayer, Player newPlayer, boolean copyFlightState,
                                     Function<Player, Flight> flights) {
        if (copyFlightState) {
            flights.apply(newPlayer).clone(flights.apply(oldPlayer));
        }
    }

    // 向玩家自己同步飞行状态（登录/重生/换维度后）
    public static void syncSelf(Player player, Function<Player, Flight> flights) {
        flights.apply(player).sync(Flight.PlayerSet.ofSelf());
    }

    // 新追踪者加入时，向该玩家同步目标的飞行状态
    public static void syncTrackingPlayer(Entity target, ServerPlayer trackingPlayer, Function<Player, Flight> flights) {
        ifPlayer(target, flights, (player, flight) -> flight.sync(Flight.PlayerSet.ofPlayer(trackingPlayer)));
    }
}
