package cc.lvjia.wings;

import cc.lvjia.wings.server.ServerEventHandler;
import cc.lvjia.wings.server.apparatus.BuffedFlightApparatus;
import cc.lvjia.wings.server.apparatus.FlightApparatus;
import cc.lvjia.wings.server.apparatus.SimpleFlightApparatus;
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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("null")
public final class WingsMod implements ModInitializer {
    public static final String ID = "wings";
    public static final @NonNull ResourceKey<Registry<FlightApparatus>> WINGS_KEY = Objects.requireNonNull(
            ResourceKey.createRegistryKey(locate("wings")), "wings registry key");
    public static final @NonNull Registry<FlightApparatus> WINGS = createWingsRegistry();
    public static final @NonNull FlightApparatus NONE = registerWing(Names.NONE, FlightApparatus.NONE);
    public static final @NonNull FlightApparatus WINGLESS = registerWing(Names.WINGLESS, new FlightApparatus() {
        @Override
        public void onFlight(Player player, Vec3 direction) {
            FlightApparatus.NONE.onFlight(player, direction);
        }

        @Override
        public void onLanding(Player player, Vec3 direction) {
            FlightApparatus.NONE.onLanding(player, direction);
        }

        @Override
        public boolean isUsable(Player player) {
            return FlightApparatus.NONE.isUsable(player);
        }

        @Override
        public boolean isLandable(Player player) {
            return FlightApparatus.NONE.isLandable(player);
        }

        @Override
        public FlightApparatus.FlightState createState(Flight flight) {
            return FlightApparatus.NONE.createState(flight);
        }
    });
    public static final @NonNull FlightApparatus ANGEL_WINGS = registerWing(Names.ANGEL, new SimpleFlightApparatus(WingsItemsConfig.ANGEL));
    public static final @NonNull FlightApparatus PARROT_WINGS = registerWing(Names.PARROT, new SimpleFlightApparatus(WingsItemsConfig.PARROT));
    public static final @NonNull FlightApparatus BAT_WINGS = registerWing(Names.BAT, new SimpleFlightApparatus(WingsItemsConfig.BAT));
    public static final @NonNull FlightApparatus BLUE_BUTTERFLY_WINGS = registerWing(Names.BLUE_BUTTERFLY, new SimpleFlightApparatus(WingsItemsConfig.BLUE_BUTTERFLY));
    public static final @NonNull FlightApparatus DRAGON_WINGS = registerWing(Names.DRAGON, new SimpleFlightApparatus(WingsItemsConfig.DRAGON));
    public static final @NonNull FlightApparatus EVIL_WINGS = registerWing(Names.EVIL, new SimpleFlightApparatus(WingsItemsConfig.EVIL));
    public static final @NonNull FlightApparatus FAIRY_WINGS = registerWing(Names.FAIRY, new SimpleFlightApparatus(WingsItemsConfig.FAIRY));
    public static final @NonNull FlightApparatus MONARCH_BUTTERFLY_WINGS = registerWing(Names.MONARCH_BUTTERFLY, new SimpleFlightApparatus(WingsItemsConfig.MONARCH_BUTTERFLY));
    public static final @NonNull FlightApparatus SLIME_WINGS = registerWing(Names.SLIME, new SimpleFlightApparatus(WingsItemsConfig.SLIME));
    public static final @NonNull FlightApparatus FIRE_WINGS = registerWing(Names.FIRE, new SimpleFlightApparatus(WingsItemsConfig.FIRE));
    public static final @NonNull FlightApparatus LVJIA_SUPER_WINGS = registerWing(Names.LVJIA_SUPER,
            new BuffedFlightApparatus(WingsItemsConfig.LVJIA_SUPER,
                    BuffedFlightApparatus.EffectSettings.of(MobEffects.RESISTANCE, 2, 40, 40),
                    BuffedFlightApparatus.EffectSettings.of(MobEffects.JUMP_BOOST, 1, 40, 40)));
    private static @Nullable WingsMod INSTANCE;
    private Proxy proxy = new Proxy();

    public WingsMod() {
        if (INSTANCE != null) throw new IllegalStateException("Already constructed!");
        INSTANCE = this;
    }

    public static WingsMod instance() {
        return Objects.requireNonNull(INSTANCE, "WingsMod not constructed");
    }

    public static @NonNull Identifier locate(@NonNull String name) {
        Identifier id = Identifier.tryBuild(WingsMod.ID, name);
        if (id == null) {
            throw new IllegalArgumentException("Invalid resource path: " + name);
        }
        return id;
    }

    private static @NonNull Registry<FlightApparatus> createWingsRegistry() {
        return Objects.requireNonNull(FabricRegistryBuilder
                .createDefaulted(WINGS_KEY, Names.NONE)
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
        ServerEventHandler.register();
        this.proxy.init();
    }

    public void setProxy(Proxy proxy) {
        this.proxy = Objects.requireNonNull(proxy, "proxy");
    }

    public void addFlightListeners(Player player, Flight instance) {
        this.requireProxy().addFlightListeners(player, instance);
    }

    public void invalidateFlightView(Player player) {
        this.requireProxy().invalidateFlightView(player);
    }

    private Proxy requireProxy() {
        return this.proxy;
    }

    public static final class Names {
        public static final @NonNull Identifier
                NONE = create("none"),
                WINGLESS = create("wingless"),
                ANGEL = create("angel_wings"),
                PARROT = create("parrot_wings"),
                SLIME = create("slime_wings"),
                BLUE_BUTTERFLY = create("blue_butterfly_wings"),
                MONARCH_BUTTERFLY = create("monarch_butterfly_wings"),
                FIRE = create("fire_wings"),
                BAT = create("bat_wings"),
                FAIRY = create("fairy_wings"),
                EVIL = create("evil_wings"),
                DRAGON = create("dragon_wings"),
                LVJIA_SUPER = create("lvjia_super_wing");

        private Names() {
        }

        private static @NonNull Identifier create(@NonNull String path) {
            Identifier id = Identifier.tryBuild(ID, path);
            if (id == null) {
                throw new IllegalArgumentException("Invalid resource path: " + path);
            }
            return id;
        }
    }
}
