package com.toni.wings.client.debug;

import com.toni.wings.WingsMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WingsMod.ID)
public final class DebugFlightAnimation {
    private DebugFlightAnimation() {
    }

    private static State state = new DisabledState();

    @SubscribeEvent
    public static void init(ModelEvent.RegisterAdditional event) {
        state = state.init();
    }

    private interface State {
        State init();
    }

    protected static final class DisabledState implements State {
        @Override
        public State init() {
            return this;
        }
    }
}
