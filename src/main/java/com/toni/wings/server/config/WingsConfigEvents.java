package com.toni.wings.server.config;

import com.toni.wings.WingsMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = WingsMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
        Object spec = config.getSpec();
        if (spec == WingsConfig.SPEC) {
            WingsConfig.validate();
        } else if (spec == WingsItemsConfig.SPEC) {
            WingsItemsConfig.validate();
        } else if (spec == WingsOreConfig.SPEC) {
            WingsOreConfig.validate();
        }
    }
}
