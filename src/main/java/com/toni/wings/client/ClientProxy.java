package com.toni.wings.client;

import com.toni.wings.Proxy;
import com.toni.wings.WingsMod;
import com.toni.wings.client.apparatus.WingForm;
import com.toni.wings.client.flight.Animator;
import com.toni.wings.client.flight.AnimatorAvian;
import com.toni.wings.client.flight.AnimatorInsectoid;
import com.toni.wings.client.flight.FlightView;
import com.toni.wings.client.model.ModelWings;
import com.toni.wings.client.model.ModelWingsAvian;
import com.toni.wings.client.model.ModelWingsInsectoid;
import com.toni.wings.client.renderer.LayerWings;
import com.toni.wings.server.flight.Flight;
import com.toni.wings.server.flight.Flights;
import com.toni.wings.server.item.BatBloodBottleItem;
import com.toni.wings.server.net.serverbound.MessageControlFlying;
import com.toni.wings.util.KeyInputListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

public final class ClientProxy extends Proxy {

    private static ModelWings<AnimatorInsectoid> insectoidWings;
    private static ModelWings<AnimatorAvian> avianWings;


    @Override
    public void init(IEventBus modBus) {
        super.init(modBus);
    LayerWings.init(modBus);
        //modBus.register(BakeModels.class);
        MinecraftForge.EVENT_BUS.register(KeyInputListener.builder()
            .category("key.categories.wings")
            .key("key.wings.fly", KeyConflictContext.IN_GAME, KeyModifier.NONE, GLFW.GLFW_KEY_R)
            .onPress(() -> {
                Player player = Minecraft.getInstance().player;
                Flights.get(player).filter(flight -> flight.canFly(player)).ifPresent(flight ->
                    flight.toggleIsFlying(Flight.PlayerSet.ofOthers())
                );
                Flights.ifPlayer(player, (p, flight) -> {
                    if (flight.getWing().equals(WingsMod.WINGLESS) && !flight.isFlying()) {
                        BatBloodBottleItem.removeWings(player);
                    }
                });
            })
            .build()
        );

    modBus.addListener(KeyInputListener::registerKeyMappings);
    }

    @Override
    protected void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        super.registerCapabilities(event);
        event.register(FlightView.class);
    }

    @Override
    public void addFlightListeners(Player player, Flight flight) {
        super.addFlightListeners(player, flight);
        if (player.isLocalPlayer()) {
            Flight.Notifier notifier = Flight.Notifier.of(
                () -> {
                },
                p -> {
                },
                () -> this.network.sendToServer(new MessageControlFlying(flight.isFlying()))
            );
            flight.registerSyncListener(players -> players.notify(notifier));
        }
    }

    static WingForm<AnimatorAvian> createAvianWings(ResourceLocation name) {
        avianWings = new ModelWingsAvian(getModel().bakeLayer(LayerWings.AVIAN_WINGS));
        return ClientProxy.createWings(name, AnimatorAvian::new, avianWings);
    }

    static WingForm<AnimatorAvian> createEndPortalWings(ResourceLocation name) {
        avianWings = new ModelWingsAvian(getModel().bakeLayer(LayerWings.AVIAN_WINGS));
        return ClientProxy.createWings(name, AnimatorAvian::new, avianWings, RenderType::endPortal);
    }


    static WingForm<AnimatorInsectoid> createInsectoidWings(ResourceLocation name) {
        insectoidWings = new ModelWingsInsectoid(getModel().bakeLayer(LayerWings.INSECTOID_WINGS));
        return ClientProxy.createWings(name, AnimatorInsectoid::new, insectoidWings);
    }

    private static  <A extends Animator> WingForm<A> createWings(ResourceLocation name, Supplier<A> animator, ModelWings<A> model) {
        return createWings(name, animator, model, null);
    }

    private static  <A extends Animator> WingForm<A> createWings(ResourceLocation name, Supplier<A> animator, ModelWings<A> model, Supplier<RenderType> renderType) {
        String texturePath = String.format("textures/entity/%s.png", name.getPath());
        ResourceLocation texture = ResourceLocation.tryBuild(name.getNamespace(), texturePath);
        if (texture == null) {
            throw new IllegalArgumentException("Invalid texture path: " + texturePath);
        }
        Supplier<RenderType> actualRenderType = renderType != null ? renderType : () -> RenderType.entityCutout(texture);
        return WingForm.of(
            animator,
            model,
            texture,
            actualRenderType
        );
    }

    @OnlyIn(Dist.CLIENT)
    private static net.minecraft.client.model.geom.EntityModelSet getModel()
    {
        return net.minecraft.client.Minecraft.getInstance().getEntityModels();
    }
}
