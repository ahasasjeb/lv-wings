package cc.lvjia.wings.client.flight.state;

import cc.lvjia.wings.client.flight.Animator;

public final class StateFall extends State {
    public StateFall() {
        super(Animator::beginFall);
    }

    @Override
    protected State createFall() {
        return this;
    }
}
