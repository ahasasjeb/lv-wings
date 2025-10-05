package com.toni.wings.client;

import com.toni.wings.WingsMod;
import net.minecraftforge.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WingsMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class WingsModels {
    private WingsModels() {
    }

    @SubscribeEvent
    public static void onRegister(ModelEvent.ModifyBakingResult event) {
    }
}
