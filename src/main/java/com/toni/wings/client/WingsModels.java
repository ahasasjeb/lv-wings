package com.toni.wings.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

public final class WingsModels {
    private WingsModels() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(WingsModels::onRegister);
    }

    @SubscribeEvent
    public static void onRegister(ModelEvent.ModifyBakingResult event) {
    }
}
