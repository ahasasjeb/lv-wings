package com.toni.wings.client.debug;

import com.toni.wings.WingsMod;
import net.minecraftforge.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WingsMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DebugFlightAnimation {
    private DebugFlightAnimation() {
    }

    private static State state = new DisabledState();

    @SubscribeEvent
    public static void init(ModelEvent.ModifyBakingResult event) {
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
