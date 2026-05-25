package cc.lvjia.wings.client.flight.state;

import cc.lvjia.wings.client.flight.Animator;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.FlightAnimationRules;
import net.minecraft.world.entity.player.Player;

public final class StateIdle extends State {
    public StateIdle() {
        super(Animator::beginIdle);
    }

    @Override
    protected State createIdle() {
        return this;
    }

    @Override
    protected State getDescent(Flight flight, Player player) {
        if (!FlightAnimationRules.isNearGround(player)) {
            return super.getDescent(flight, player);
        }
        return this.createIdle();
    }
}
