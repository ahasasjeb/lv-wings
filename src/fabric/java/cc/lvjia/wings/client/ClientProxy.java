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
import cc.lvjia.wings.server.apparatus.FlightApparatus;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.Flights;
import cc.lvjia.wings.server.item.BatBloodBottleItem;
import cc.lvjia.wings.server.net.Message;
import cc.lvjia.wings.server.net.serverbound.MessageControlFlying;
import cc.lvjia.wings.util.KeyInputListener;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.function.Supplier;

public final class ClientProxy extends Proxy {
    private static final Logger LOGGER = LogManager.getLogger("WingsClient");

    private static final KeyMapping.Category WINGS_KEY_CATEGORY = new KeyMapping.Category(WingsMod.locate("wings"));

    public static void registerWingForms(@NonNull EntityModelSet modelSet) {
        WingForm.clear();
        WingForm.register(WingsMod.ANGEL_WINGS,
                createAvianWings(modelSet, wingKey(WingsMod.ANGEL_WINGS)));
        WingForm.register(WingsMod.PARROT_WINGS,
                createAvianWings(modelSet, wingKey(WingsMod.PARROT_WINGS)));
        WingForm.register(WingsMod.BAT_WINGS, createAvianWings(modelSet, wingKey(WingsMod.BAT_WINGS)));
        WingForm.register(WingsMod.BLUE_BUTTERFLY_WINGS,
                createInsectoidWings(modelSet, wingKey(WingsMod.BLUE_BUTTERFLY_WINGS)));
        WingForm.register(WingsMod.DRAGON_WINGS,
                createAvianWings(modelSet, wingKey(WingsMod.DRAGON_WINGS)));
        WingForm.register(WingsMod.EVIL_WINGS, createAvianWings(modelSet, wingKey(WingsMod.EVIL_WINGS)));
        WingForm.register(WingsMod.FAIRY_WINGS,
                createInsectoidWings(modelSet, wingKey(WingsMod.FAIRY_WINGS)));
        WingForm.register(WingsMod.FIRE_WINGS, createAvianWings(modelSet, wingKey(WingsMod.FIRE_WINGS)));
        WingForm.register(WingsMod.MONARCH_BUTTERFLY_WINGS,
                createInsectoidWings(modelSet, wingKey(WingsMod.MONARCH_BUTTERFLY_WINGS)));
        WingForm.register(WingsMod.SLIME_WINGS,
                createInsectoidWings(modelSet, wingKey(WingsMod.SLIME_WINGS)));
        WingForm.register(WingsMod.LVJIA_SUPER_WINGS,
                createEndPortalWings(modelSet, wingKey(WingsMod.LVJIA_SUPER_WINGS)));
    }

    static @NonNull WingForm<@NonNull AnimatorAvian> createAvianWings(@NonNull EntityModelSet modelSet, @NonNull Identifier name) {
        return ClientProxy.createWings(name, AnimatorAvian::new,
                new ModelWingsAvian(modelSet.bakeLayer(LayerWings.AVIAN_WINGS)));
    }

    static @NonNull WingForm<@NonNull AnimatorAvian> createEndPortalWings(@NonNull EntityModelSet modelSet, @NonNull Identifier name) {
        return ClientProxy.createWings(name, AnimatorAvian::new,
                new ModelWingsAvian(modelSet.bakeLayer(LayerWings.AVIAN_WINGS)), RenderTypes::endPortal);
    }

    static @NonNull WingForm<@NonNull AnimatorInsectoid> createInsectoidWings(@NonNull EntityModelSet modelSet, @NonNull Identifier name) {
        return ClientProxy.createWings(name, AnimatorInsectoid::new,
                new ModelWingsInsectoid(modelSet.bakeLayer(LayerWings.INSECTOID_WINGS)));
    }

    private static <A extends @NonNull Animator> @NonNull WingForm<A> createWings(@NonNull Identifier name,
            @NonNull Supplier<@NonNull A> animator, @NonNull ModelWings<A> model) {
        String texturePath = "textures/entity/" + name.getPath() + ".png";
        Identifier texture = Objects.requireNonNull(
                Identifier.tryBuild(Objects.requireNonNull(name.getNamespace(), "namespace"), texturePath),
                "Invalid texture path: " + texturePath);
        return WingForm.of(
                animator,
                model,
                texture);
    }

    private static <A extends @NonNull Animator> @NonNull WingForm<A> createWings(@NonNull Identifier name,
            @NonNull Supplier<@NonNull A> animator, @NonNull ModelWings<A> model,
            @NonNull Supplier<@NonNull RenderType> renderType) {
        String texturePath = "textures/entity/" + name.getPath() + ".png";
        Identifier texture = Objects.requireNonNull(
                Identifier.tryBuild(Objects.requireNonNull(name.getNamespace(), "namespace"), texturePath),
                "Invalid texture path: " + texturePath);
        return WingForm.of(
                animator,
                model,
                texture,
                renderType);
    }

    private static @NonNull Identifier wingKey(FlightApparatus wing) {
        return Objects.requireNonNull(WingsMod.WINGS.getKey(wing), "Wing is not registered: " + wing);
    }

    public void initClient() {
        this.network.registerClient();
        LayerWings.init();
        ClientEventHandler.register();
        KeyInputListener.builder()
                .category(WINGS_KEY_CATEGORY)
                .key("key.wings.fly", GLFW.GLFW_KEY_R)
                .onPress(() -> {
                    Player player = Minecraft.getInstance().player;
                    if (player == null || player.isSpectator()) {
                        return;
                    }
                    Flights.get(player).toggleIsFlying(Flight.PlayerSet.ofOthers());
                    Flights.ifPlayer(player, (p, flight) -> {
                        if (flight.getWing().equals(WingsMod.WINGLESS) && !flight.isFlying()) {
                            BatBloodBottleItem.removeWings(player);
                        }
                    });
                })
                .build()
                .register();
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
                    () -> this.sendToServer(new MessageControlFlying(flight.isFlying())));
            flight.registerSyncListener(players -> players.notify(notifier));
        }
    }

    @Override
    public void sendToServer(Message message) {
        LOGGER.debug("Sending {} to server", message.type().id());
        ClientPlayNetworking.send(message);
    }

    @Override
    public void invalidateFlightView(Player player) {
        if (player instanceof AbstractClientPlayer clientPlayer) {
            FlightViews.invalidate(clientPlayer);
        }
    }
}
