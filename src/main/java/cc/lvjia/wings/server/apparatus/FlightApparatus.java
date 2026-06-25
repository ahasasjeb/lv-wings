package cc.lvjia.wings.server.apparatus;

import cc.lvjia.wings.server.flight.Flight;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

/**
 * 飞行“装置/能力”接口。
 * <p>
 * 用于在飞行/降落过程中挂接额外行为（粒子、音效、属性修改等），并为飞行系统提供状态更新。
 */
public interface FlightApparatus {
    // 空实现：所有方法无操作，isUsable/isLandable 恒为 true
    @NonNull FlightApparatus NONE = new FlightApparatus() {
        @Override
        public void onFlight(@NonNull Player player, @NonNull Vec3 direction) {
        }

        @Override
        public void onLanding(@NonNull Player player, @NonNull Vec3 direction) {
        }

        @Override
        public boolean isUsable(@NonNull Player player) {
            return true;
        }

        @Override
        public boolean isLandable(@NonNull Player player) {
            return true;
        }

        @Override
        public @NonNull FlightState createState(@NonNull Flight flight) {
            return FlightState.NONE;
        }
    };

    // 飞行中每 tick 触发（粒子/音效/属性面板）
    void onFlight(@NonNull Player player, @NonNull Vec3 direction);

    // 着陆时触发
    void onLanding(@NonNull Player player, @NonNull Vec3 direction);

    // 当前翅膀能否使用（检查条件如饥饿值、环境等）
    boolean isUsable(@NonNull Player player);

    // 当前翅膀能否着陆（某些翅膀在空中不可着陆）
    boolean isLandable(@NonNull Player player);

    // 创建每 tick 更新的飞行状态对象
    @NonNull FlightState createState(@NonNull Flight flight);

    // 每 tick 更新的飞行状态，由 createState 创建，用于持续行为（如持续粒子）
    interface FlightState {
        @NonNull FlightState NONE = (player) -> {
        };

        void onUpdate(@NonNull Player player);
    }
}
