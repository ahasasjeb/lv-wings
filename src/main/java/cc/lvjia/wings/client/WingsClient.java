package cc.lvjia.wings.client;

import cc.lvjia.wings.WingsMod;
import net.fabricmc.api.ClientModInitializer;

public final class WingsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientProxy proxy = new ClientProxy();
        WingsMod.instance().setProxy(proxy);
        proxy.initClient();
    }
}
