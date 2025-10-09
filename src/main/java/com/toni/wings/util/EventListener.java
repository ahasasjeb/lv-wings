package com.toni.wings.util;

import com.toni.wings.WingsMod;
import com.toni.wings.client.ReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = WingsMod.ID)
public class EventListener {

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ReloadListener());
    }

    @SubscribeEvent
    public static void renderLiving(EntityRenderersEvent.RegisterRenderers event) {

    }

}
