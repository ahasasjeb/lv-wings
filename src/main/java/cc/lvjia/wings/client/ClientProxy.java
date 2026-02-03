package cc.lvjia.wings.client;

import cc.lvjia.wings.Proxy;
import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.client.apparatus.WingForm;
import cc.lvjia.wings.client.flight.Animator;
import cc.lvjia.wings.client.flight.AnimatorAvian;
import cc.lvjia.wings.client.flight.AnimatorInsectoid;
import cc.lvjia.wings.client.flight.FlightViews;
import cc.lvjia.wings.client.model.ModelWings;
import cc.lvjia.wings.client.model.ModelWingsAvian;
import cc.lvjia.wings.client.model.ModelWingsInsectoid;
import cc.lvjia.wings.client.renderer.LayerWings;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.Flights;
import cc.lvjia.wings.server.item.BatBloodBottleItem;
import cc.lvjia.wings.server.net.serverbound.MessageControlFlying;
import cc.lvjia.wings.server.net.Message;
import cc.lvjia.wings.util.KeyInputListener;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

public final class ClientProxy extends Proxy {

    private static final KeyMapping.Category WINGS_KEY_CATEGORY = new KeyMapping.Category(WingsMod.locate("wings"));
    private static ModelWings<AnimatorInsectoid> insectoidWings;
    private static ModelWings<AnimatorAvian> avianWings;

    static WingForm<AnimatorAvian> createAvianWings(Identifier name) {
        avianWings = new ModelWingsAvian(getModel().bakeLayer(LayerWings.AVIAN_WINGS));
        return ClientProxy.createWings(name, AnimatorAvian::new, avianWings);
    }

    static WingForm<AnimatorAvian> createEndPortalWings(Identifier name) {
        avianWings = new ModelWingsAvian(getModel().bakeLayer(LayerWings.AVIAN_WINGS));
        return ClientProxy.createWings(name, AnimatorAvian::new, avianWings, RenderTypes::endPortal);
    }

    static WingForm<AnimatorInsectoid> createInsectoidWings(Identifier name) {
        insectoidWings = new ModelWingsInsectoid(getModel().bakeLayer(LayerWings.INSECTOID_WINGS));
        return ClientProxy.createWings(name, AnimatorInsectoid::new, insectoidWings);
    }

    private static <A extends Animator> WingForm<A> createWings(Identifier name, Supplier<A> animator, ModelWings<A> model) {
        return createWings(name, animator, model, null);
    }

    private static <A extends Animator> WingForm<A> createWings(Identifier name, Supplier<A> animator, ModelWings<A> model, Supplier<RenderType> renderType) {
        String texturePath = String.format("textures/entity/%s.png", name.getPath());
        Identifier texture = Identifier.tryBuild(name.getNamespace(), texturePath);
        if (texture == null) {
            throw new IllegalArgumentException("Invalid texture path: " + texturePath);
        }
        Supplier<RenderType> actualRenderType = renderType != null ? renderType : () -> RenderTypes.entityCutout(texture);
        return WingForm.of(
                animator,
                model,
                texture,
                actualRenderType
        );
    }

    private static net.minecraft.client.model.geom.EntityModelSet getModel() {
        return net.minecraft.client.Minecraft.getInstance().getEntityModels();
    }

    @Override
    public void init(IEventBus modBus) {
        super.init(modBus);
        LayerWings.init(modBus);
        WingsModels.init(modBus);
        NeoForge.EVENT_BUS.register(KeyInputListener.builder()
                .category(WINGS_KEY_CATEGORY)
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
    public void addFlightListeners(Player player, Flight flight) {
        super.addFlightListeners(player, flight);
        if (player.isLocalPlayer()) {
            Flight.Notifier notifier = Flight.Notifier.of(
                    () -> {
                    },
                    p -> {
                    },
                    () -> this.sendToServer(new MessageControlFlying(flight.isFlying()))
            );
            flight.registerSyncListener(players -> players.notify(notifier));
        }
    }

    @Override
    public void sendToServer(Message message) {
        ClientPacketDistributor.sendToServer(message);
    }

    @Override
    public void invalidateFlightView(Player player) {
        if (player instanceof AbstractClientPlayer clientPlayer) {
            FlightViews.invalidate(clientPlayer);
        }
    }
}
