package cc.lvjia.wings.server.flight;

import cc.lvjia.wings.server.apparatus.FlightApparatus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

// 飞行状态核心接口，定义状态管理、物理模拟、网络同步与序列化契约
public interface Flight {
    default void setIsFlying(boolean isFlying) {
        this.setIsFlying(isFlying, PlayerSet.empty());
    }

    // 设置飞行状态并同步到指定玩家集
    void setIsFlying(boolean isFlying, PlayerSet players);

    boolean isFlying();

    // 切换飞行状态（开→关 / 关→开）
    default void toggleIsFlying(PlayerSet players) {
        this.setIsFlying(!this.isFlying(), players);
    }

    int getTimeFlying();

    void setTimeFlying(int timeFlying);

    // 切换翅膀类型并同步
    void setWing(FlightApparatus wing, PlayerSet players);

    FlightApparatus getWing();

    // 便捷方法：切换翅膀不触发网络同步
    default void setWing(FlightApparatus wing) {
        this.setWing(wing, PlayerSet.empty());
    }

    FlightAnimationState getAnimationState();

    void setAnimationState(FlightAnimationState animationState);

    // 获取翅膀展开/收起的插值量 (0~1)，delta 为帧间时间
    float getFlyingAmount(float delta);

    void registerFlyingListener(FlyingListener listener);

    void registerSyncListener(SyncListener listener);

    boolean canFly(Player player);

    boolean hasEffect(Player player);

    boolean canLand(Player player);

    // 每 tick 更新飞行物理和状态
    void tick(Player player);

    // 玩家挥动翅膀时调用（触发粒子/音效）
    void onFlown(Player player, Vec3 direction);

    // 从旧 Flight 复制状态（克隆/维度切换时）
    void clone(Flight other);

    // 派发同步请求到指定玩家集
    void sync(PlayerSet players);

    void serialize(FriendlyByteBuf buf);

    void deserialize(FriendlyByteBuf buf);

    interface FlyingListener {
        static Consumer<FlyingListener> onChangeUsing(boolean isFlying) {
            return l -> l.onChange(isFlying);
        }

        void onChange(boolean isFlying);
    }

    /**
     * 同步回调只描述“把当前状态推给谁”，不关心具体怎么发包。
     */
    interface SyncListener {
        static Consumer<SyncListener> onSyncUsing(PlayerSet players) {
            return l -> l.onSync(players);
        }

        void onSync(PlayerSet players);
    }

    /**
     * 一次同步要发送到的目标集合。
     * <p>
     * 这样上层只需要表达“自己 / 某个玩家 / 追踪者 / 全部”，
     * 具体的网络发送细节由监听器统一处理。
     */
    interface PlayerSet {
        static PlayerSet empty() {
            return n -> {
            };
        }

        static PlayerSet ofSelf() {
            return Notifier::notifySelf;
        }

        static PlayerSet ofPlayer(ServerPlayer player) {
            return n -> n.notifyPlayer(player);
        }

        static PlayerSet ofOthers() {
            return Notifier::notifyOthers;
        }

        static PlayerSet ofAll() {
            return n -> {
                n.notifySelf();
                n.notifyOthers();
            };
        }

        void notify(Notifier notifier);
    }

    interface Notifier {
        static Notifier of(Runnable notifySelf, Consumer<ServerPlayer> notifyPlayer, Runnable notifyOthers) {
            return new Notifier() {
                @Override
                public void notifySelf() {
                    notifySelf.run();
                }

                @Override
                public void notifyPlayer(ServerPlayer player) {
                    notifyPlayer.accept(player);
                }

                @Override
                public void notifyOthers() {
                    notifyOthers.run();
                }
            };
        }

        void notifySelf();

        void notifyPlayer(ServerPlayer player);

        void notifyOthers();
    }
}
