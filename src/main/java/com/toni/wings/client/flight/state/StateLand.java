package com.toni.wings.client.flight.state;

import com.toni.wings.client.flight.Animator;

public final class StateLand extends State {
    private static final int LAND_STATE_DELAY = 10;

    public StateLand() {
        super(LAND_STATE_DELAY, Animator::beginLand);
    }

    @Override
    protected State createLand() {
        return this;
    }
}
