package com.toni.wings.util;

import com.toni.wings.WingsMod;
import com.toni.wings.client.ReloadListener;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WingsMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EventListener {

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event){
        event.registerReloadListener(new ReloadListener());
    }

    @SubscribeEvent
    public static void renderLiving(EntityRenderersEvent.RegisterRenderers event) {

    }

}
