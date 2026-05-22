package com.toni.wings.util;

import com.toni.wings.WingsMod;
import com.toni.wings.client.ReloadListener;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WingsMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class EventListener {
    private EventListener() {
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event){
        event.registerReloadListener(new ReloadListener());
    }
}
