package cc.lvjia.wings;

import cc.lvjia.wings.client.ClientProxy;
import cc.lvjia.wings.server.ServerProxy;
import cc.lvjia.wings.server.apparatus.BuffedFlightApparatus;
import cc.lvjia.wings.server.apparatus.FlightApparatus;
import cc.lvjia.wings.server.apparatus.SimpleFlightApparatus;
import cc.lvjia.wings.server.command.WingsArgument;
import cc.lvjia.wings.server.config.WingsConfig;
import cc.lvjia.wings.server.config.WingsItemsConfig;
import cc.lvjia.wings.server.config.WingsOreConfig;
import cc.lvjia.wings.server.dreamcatcher.InSomniableCapability;
import cc.lvjia.wings.server.effect.WingsEffects;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.Flights;
import cc.lvjia.wings.server.item.WingsItems;
import cc.lvjia.wings.server.sound.WingsSounds;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(WingsMod.ID)
public final class WingsMod {
    public static final String ID = "wings";
    public static final Registry<FlightApparatus> WINGS = new DefaultedMappedRegistry<>(Names.NONE.toString(), ResourceKey.createRegistryKey(locate("wings")), Lifecycle.experimental(), false);
    public static final FlightApparatus NONE = Registry.register(WINGS, Names.NONE, FlightApparatus.NONE);
    public static final FlightApparatus WINGLESS = Registry.register(WINGS, Names.WINGLESS, new FlightApparatus() {
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
    public static final FlightApparatus ANGEL_WINGS = Registry.register(WINGS, Names.ANGEL, new SimpleFlightApparatus(WingsItemsConfig.ANGEL));
    public static final FlightApparatus PARROT_WINGS = Registry.register(WINGS, Names.PARROT, new SimpleFlightApparatus(WingsItemsConfig.PARROT));
    public static final FlightApparatus BAT_WINGS = Registry.register(WINGS, Names.BAT, new SimpleFlightApparatus(WingsItemsConfig.BAT));
    public static final FlightApparatus BLUE_BUTTERFLY_WINGS = Registry.register(WINGS, Names.BLUE_BUTTERFLY, new SimpleFlightApparatus(WingsItemsConfig.BLUE_BUTTERFLY));
    public static final FlightApparatus DRAGON_WINGS = Registry.register(WINGS, Names.DRAGON, new SimpleFlightApparatus(WingsItemsConfig.DRAGON));
    public static final FlightApparatus EVIL_WINGS = Registry.register(WINGS, Names.EVIL, new SimpleFlightApparatus(WingsItemsConfig.EVIL));
    public static final FlightApparatus FAIRY_WINGS = Registry.register(WINGS, Names.FAIRY, new SimpleFlightApparatus(WingsItemsConfig.FAIRY));
    public static final FlightApparatus MONARCH_BUTTERFLY_WINGS = Registry.register(WINGS, Names.MONARCH_BUTTERFLY, new SimpleFlightApparatus(WingsItemsConfig.MONARCH_BUTTERFLY));
    public static final FlightApparatus SLIME_WINGS = Registry.register(WINGS, Names.SLIME, new SimpleFlightApparatus(WingsItemsConfig.SLIME));
    public static final FlightApparatus FIRE_WINGS = Registry.register(WINGS, Names.FIRE, new SimpleFlightApparatus(WingsItemsConfig.FIRE));
    public static final FlightApparatus LVJIA_SUPER_WINGS = Registry.register(WINGS, Names.LVJIA_SUPER,
            (FlightApparatus) new BuffedFlightApparatus(WingsItemsConfig.LVJIA_SUPER,
                    BuffedFlightApparatus.EffectSettings.of(MobEffects.RESISTANCE, 2, 40, 40),
                    BuffedFlightApparatus.EffectSettings.of(MobEffects.JUMP_BOOST, 1, 40, 40)));
    private static final DeferredRegister<net.minecraft.commands.synchronization.ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.COMMAND_ARGUMENT_TYPE, ID);
    public static final DeferredHolder<net.minecraft.commands.synchronization.ArgumentTypeInfo<?, ?>, net.minecraft.commands.synchronization.SingletonArgumentInfo<WingsArgument>> WINGS_ARGUMENT_TYPE =
            COMMAND_ARGUMENT_TYPES.register("wings", () ->
                    net.minecraft.commands.synchronization.ArgumentTypeInfos.registerByClass(
                            WingsArgument.class,
                            net.minecraft.commands.synchronization.SingletonArgumentInfo.contextFree(WingsArgument::wings)
                    )
            );
    private static WingsMod INSTANCE;
    private final Proxy proxy;

    public WingsMod(IEventBus modEventBus, ModContainer modContainer) {
        if (INSTANCE != null) throw new IllegalStateException("Already constructed!");
        INSTANCE = this;

        WingsAttachments.register(modEventBus);
        modEventBus.addListener(Flights::registerCapabilities);
        modEventBus.addListener(InSomniableCapability::registerCapabilities);

        modContainer.registerConfig(ModConfig.Type.COMMON, WingsConfig.SPEC, ID + "-common.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, WingsItemsConfig.SPEC, ID + "-items.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, WingsOreConfig.SPEC, ID + "-ores.toml");

        WingsItems.REG.register(modEventBus);
        modEventBus.addListener(WingsItems::buildCreativeTabContents);
        WingsSounds.REG.register(modEventBus);
        WingsEffects.REG.register(modEventBus);
        COMMAND_ARGUMENT_TYPES.register(modEventBus);

        this.proxy = FMLEnvironment.getDist().isClient() ? new ClientProxy() : new ServerProxy();
        this.proxy.init(modEventBus);
    }

    public static WingsMod instance() {
        return INSTANCE;
    }

    public static Identifier locate(String name) {
        Identifier location = Identifier.tryBuild(WingsMod.ID, name);
        if (location == null) {
            throw new IllegalArgumentException("Invalid resource path: " + name);
        }
        return location;
    }

    public void addFlightListeners(Player player, Flight instance) {
        this.requireProxy().addFlightListeners(player, instance);
    }

    public void invalidateFlightView(Player player) {
        this.requireProxy().invalidateFlightView(player);
    }

    private Proxy requireProxy() {
        if (this.proxy == null) {
            throw new IllegalStateException("Proxy not initialized");
        }
        return this.proxy;
    }

    public static final class Names {
        public static final Identifier
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

        private static Identifier create(String path) {
            Identifier location = Identifier.tryBuild(ID, path);
            if (location == null) {
                throw new IllegalArgumentException("Invalid resource path: " + path);
            }
            return location;
        }
    }
}
