package com.toni.wings.client.flight.state;

import com.toni.wings.client.flight.Animator;
import com.toni.wings.server.flight.Flight;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

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
        BlockPos below = Objects.requireNonNull(BlockPos.containing(player.getX(), player.getY() - 0.25D, player.getZ()));
        BlockPos twoBelow = Objects.requireNonNull(below.below());
        if (player.level().isEmptyBlock(below) && player.level().isEmptyBlock(twoBelow)) {
            return super.getDescent(flight, player);
        }
        return this.createIdle();
    }
}
