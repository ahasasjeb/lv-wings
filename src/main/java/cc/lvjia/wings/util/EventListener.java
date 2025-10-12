package cc.lvjia.wings.util;

import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.client.ReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = WingsMod.ID)
public class EventListener {

    @SubscribeEvent
    public static void onAddClientReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(WingsMod.locate("client_reload_listener"), new ReloadListener());
    }

}
