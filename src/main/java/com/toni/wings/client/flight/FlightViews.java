package com.toni.wings.client.flight;

import com.toni.wings.WingsMod;
import com.toni.wings.server.flight.AttachFlightCapabilityEvent;
import com.toni.wings.util.CapabilityHolder;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WingsMod.ID, value = Dist.CLIENT)
public final class FlightViews {
    private FlightViews() {
    }

    private static final CapabilityHolder<LivingEntity, FlightView, CapabilityHolder.State<LivingEntity, FlightView>> HOLDER = CapabilityHolder.create();

    public static boolean has(LivingEntity player) {
        return HOLDER.state().has(player, null);
    }

    public static LazyOptional<FlightView> get(LivingEntity player) {
        return HOLDER.state().get(player, null);
    }

    public static void invalidate(LivingEntity player) {
        get(player).ifPresent(FlightView::invalidate);
    }

    /*@CapabilityInject(FlightView.class)
    static void inject(Capability<FlightView> capability) {
        HOLDER.inject(capability);
    }*/

    public static final Capability<FlightView> FLIGHT_VIEW_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    static void injectFlightView(Capability<FlightView> capability) {
        HOLDER.inject(capability);
    }


    @SubscribeEvent
    public static void onAttachCapabilities(AttachFlightCapabilityEvent event) {
        Entity entity = event.getObject();
        if (entity instanceof AbstractClientPlayer) {
            injectFlightView(FLIGHT_VIEW_CAPABILITY);
            event.addCapability(
                WingsMod.locate("flight_view"),
                HOLDER.state().providerBuilder(new FlightViewDefault((Player) entity, event.getInstance())).build()
            );
        }
    }
}
