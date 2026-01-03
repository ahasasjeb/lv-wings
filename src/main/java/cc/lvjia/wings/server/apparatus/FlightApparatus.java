package cc.lvjia.wings.server.apparatus;

import cc.lvjia.wings.server.flight.Flight;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 飞行“装置/能力”接口。
 * <p>
 * 用于在飞行/降落过程中挂接额外行为（粒子、音效、属性修改等），并为飞行系统提供状态更新。
 */
public interface FlightApparatus {
    /**
     * 空实现：不做任何额外行为。
     */
    FlightApparatus NONE = new FlightApparatus() {
        @Override
        public void onFlight(Player player, Vec3 direction) {
        }

        @Override
        public void onLanding(Player player, Vec3 direction) {
        }

        @Override
        public boolean isUsable(Player player) {
            return true;
        }

        @Override
        public boolean isLandable(Player player) {
            return true;
        }

        @Override
        public FlightState createState(Flight flight) {
            return FlightState.NONE;
        }
    };

    void onFlight(Player player, Vec3 direction);

    void onLanding(Player player, Vec3 direction);

    boolean isUsable(Player player);

    boolean isLandable(Player player);

    FlightState createState(Flight flight);

    /**
     * 每 tick 更新的飞行状态对象（通常由 {@link #createState(Flight)} 创建）。
     */
    interface FlightState {
        FlightState NONE = (player) -> {
        };

        void onUpdate(Player player);
    }
}
