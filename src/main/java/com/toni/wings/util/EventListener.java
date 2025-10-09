package com.toni.wings.util;

import com.toni.wings.WingsMod;
import com.toni.wings.client.ReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = WingsMod.ID)
public class EventListener {

    @SubscribeEvent
    public static void onAddClientReloadListeners(AddClientReloadListenersEvent event){
        event.addListener(WingsMod.locate("client_reload_listener"), new ReloadListener());
    }

}
