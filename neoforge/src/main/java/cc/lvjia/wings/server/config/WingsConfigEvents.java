package cc.lvjia.wings.server.config;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

public final class WingsConfigEvents {
    private WingsConfigEvents() {
    }

    public static void onLoad(ModConfigEvent.Loading event) {
        validate(event.getConfig());
    }

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
        }
    }
}
