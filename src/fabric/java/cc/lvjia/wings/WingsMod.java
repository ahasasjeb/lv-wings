package cc.lvjia.wings;

import cc.lvjia.wings.server.FabricServerEventHandler;
import cc.lvjia.wings.server.apparatus.FlightApparatus;
import cc.lvjia.wings.server.config.WingsConfig;
import cc.lvjia.wings.server.config.WingsItemsConfig;
import cc.lvjia.wings.server.effect.WingsEffects;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.item.WingsItems;
import cc.lvjia.wings.server.sound.WingsSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("null")
public final class WingsMod implements ModInitializer {
    public static final String ID = WingsCore.ID;
    public static final @NonNull ResourceKey<Registry<FlightApparatus>> WINGS_KEY = WingsCore.WINGS_KEY;
    public static final @NonNull Registry<FlightApparatus> WINGS = createWingsRegistry();
    private static final WingsCore.WingSet WING_SET = WingsCore.registerWings(WingsMod::registerWing);
    public static final @NonNull FlightApparatus NONE = WING_SET.none();
    public static final @NonNull FlightApparatus WINGLESS = WING_SET.wingless();
    public static final @NonNull FlightApparatus ANGEL_WINGS = WING_SET.angel();
    public static final @NonNull FlightApparatus PARROT_WINGS = WING_SET.parrot();
    public static final @NonNull FlightApparatus BAT_WINGS = WING_SET.bat();
    public static final @NonNull FlightApparatus BLUE_BUTTERFLY_WINGS = WING_SET.blueButterfly();
    public static final @NonNull FlightApparatus DRAGON_WINGS = WING_SET.dragon();
    public static final @NonNull FlightApparatus EVIL_WINGS = WING_SET.evil();
    public static final @NonNull FlightApparatus FAIRY_WINGS = WING_SET.fairy();
    public static final @NonNull FlightApparatus MONARCH_BUTTERFLY_WINGS = WING_SET.monarchButterfly();
    public static final @NonNull FlightApparatus SLIME_WINGS = WING_SET.slime();
    public static final @NonNull FlightApparatus FIRE_WINGS = WING_SET.fire();
    public static final @NonNull FlightApparatus LVJIA_SUPER_WINGS = WING_SET.lvjiaSuper();
    private static @Nullable WingsMod INSTANCE;
    private FabricProxy proxy = new FabricProxy();

    public WingsMod() {
        if (INSTANCE != null) throw new IllegalStateException("Already constructed!");
        INSTANCE = this;
    }

    public static WingsMod instance() {
        return Objects.requireNonNull(INSTANCE, "WingsMod not constructed");
    }

    public static @NonNull Identifier locate(@NonNull String name) {
        return WingsCore.locate(name);
    }

    private static @NonNull Registry<FlightApparatus> createWingsRegistry() {
        return Objects.requireNonNull(FabricRegistryBuilder
                .createDefaulted(WINGS_KEY, WingsCore.Names.NONE)
                .attribute(RegistryAttribute.SYNCED)
                .buildAndRegister(), "wings registry");
    }

    private static @NonNull FlightApparatus registerWing(@NonNull Identifier id, @NonNull FlightApparatus wing) {
        return Objects.requireNonNull(Registry.register(WINGS, id, wing), "registered wing");
    }

    @Override
    public void onInitialize() {
        WingsConfig.validate();
        WingsItemsConfig.validate();
        WingsItems.register();
        WingsSounds.register();
        WingsEffects.register();
        WingsAttachments.register();
        FabricServerEventHandler.register();
        this.proxy.init();
    }

    public void setProxy(FabricProxy proxy) {
        this.proxy = Objects.requireNonNull(proxy, "proxy");
    }

    public void addFlightListeners(Player player, Flight instance) {
        this.requireProxy().addFlightListeners(player, instance);
    }

    public void invalidateFlightView(Player player) {
        this.requireProxy().invalidateFlightView(player);
    }

    private FabricProxy requireProxy() {
        return this.proxy;
    }
}
