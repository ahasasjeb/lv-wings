package com.toni.wings.client.flight.state;

import com.toni.wings.client.flight.Animator;

public final class StateLand extends State {
    public StateLand() {
        super(Animator::beginLand);
    }

    @Override
    protected State createLand() {
        return this;
    }
}
