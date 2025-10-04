package com.toni.wings.client.flight.state;

import com.toni.wings.client.flight.Animator;
import com.toni.wings.server.flight.Flight;
import net.minecraft.core.BlockPos;
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
    BlockPos below = BlockPos.containing(player.getX(), player.getY() - 0.25D, player.getZ());
    if (player.level().isEmptyBlock(below) && player.level().isEmptyBlock(below.below())) {
            return super.getDescent(flight, player);
        }
        return this.createIdle();
    }
}
