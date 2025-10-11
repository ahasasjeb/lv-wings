package cc.lvjia.wings.server.config;

import cc.lvjia.wings.WingsMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = WingsMod.ID)
public final class WingsConfigEvents {
    private WingsConfigEvents() {
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        validate(event.getConfig());
    }

    @SubscribeEvent
    public static void onReload(ModConfigEvent.Reloading event) {
        validate(event.getConfig());
    }

    private static void validate(ModConfig config) {
        if (config == null) {
            return;
        }
        Object spec = config.getSpec();
        if (spec == null) {
            return;
        }
        if (spec == WingsConfig.SPEC) {
            WingsConfig.validate();
        } else if (spec == WingsItemsConfig.SPEC) {
            WingsItemsConfig.validate();
        } else if (spec == WingsOreConfig.SPEC) {
            WingsOreConfig.validate();
        }
    }
}
