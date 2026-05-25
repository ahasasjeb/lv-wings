package cc.lvjia.wings;

import cc.lvjia.wings.client.ClientProxy;
import cc.lvjia.wings.server.apparatus.FlightApparatus;
import cc.lvjia.wings.server.command.WingsArgument;
import cc.lvjia.wings.server.config.WingsConfig;
import cc.lvjia.wings.server.config.WingsConfigEvents;
import cc.lvjia.wings.server.config.WingsItemsConfig;
import cc.lvjia.wings.server.dreamcatcher.InSomniableCapability;
import cc.lvjia.wings.server.effect.WingsEffects;
import cc.lvjia.wings.server.flight.Flight;
import cc.lvjia.wings.server.flight.Flights;
import cc.lvjia.wings.server.item.WingsItems;
import cc.lvjia.wings.server.sound.WingsSounds;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(WingsMod.ID)
public final class WingsMod {
    public static final String ID = WingsCore.ID;
    public static final ResourceKey<Registry<FlightApparatus>> WINGS_KEY = WingsCore.WINGS_KEY;
    public static final Registry<FlightApparatus> WINGS = new DefaultedMappedRegistry<>(Names.NONE.toString(),
            WINGS_KEY, Lifecycle.experimental(), false);
    private static final WingsCore.WingSet WING_SET = WingsCore.registerWings((id, wing) ->
            Registry.register(WINGS, id, wing));
    public static final FlightApparatus NONE = WING_SET.none();
    public static final FlightApparatus WINGLESS = WING_SET.wingless();
    public static final FlightApparatus ANGEL_WINGS = WING_SET.angel();
    public static final FlightApparatus PARROT_WINGS = WING_SET.parrot();
    public static final FlightApparatus BAT_WINGS = WING_SET.bat();
    public static final FlightApparatus BLUE_BUTTERFLY_WINGS = WING_SET.blueButterfly();
    public static final FlightApparatus DRAGON_WINGS = WING_SET.dragon();
    public static final FlightApparatus EVIL_WINGS = WING_SET.evil();
    public static final FlightApparatus FAIRY_WINGS = WING_SET.fairy();
    public static final FlightApparatus MONARCH_BUTTERFLY_WINGS = WING_SET.monarchButterfly();
    public static final FlightApparatus SLIME_WINGS = WING_SET.slime();
    public static final FlightApparatus FIRE_WINGS = WING_SET.fire();
    public static final FlightApparatus LVJIA_SUPER_WINGS = WING_SET.lvjiaSuper();
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
        modEventBus.addListener(WingsConfigEvents::onLoad);
        modEventBus.addListener(WingsConfigEvents::onReload);

        WingsItems.REG.register(modEventBus);
        modEventBus.addListener(WingsItems::buildCreativeTabContents);
        WingsSounds.REG.register(modEventBus);
        WingsEffects.REG.register(modEventBus);
        COMMAND_ARGUMENT_TYPES.register(modEventBus);

        this.proxy = FMLEnvironment.getDist().isClient() ? new ClientProxy() : new Proxy();
        this.proxy.init(modEventBus);
    }

    public static WingsMod instance() {
        return INSTANCE;
    }

    public static Identifier locate(String name) {
        return WingsCore.locate(name);
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
                NONE = WingsCore.Names.NONE,
                WINGLESS = WingsCore.Names.WINGLESS,
                ANGEL = WingsCore.Names.ANGEL,
                PARROT = WingsCore.Names.PARROT,
                SLIME = WingsCore.Names.SLIME,
                BLUE_BUTTERFLY = WingsCore.Names.BLUE_BUTTERFLY,
                MONARCH_BUTTERFLY = WingsCore.Names.MONARCH_BUTTERFLY,
                FIRE = WingsCore.Names.FIRE,
                BAT = WingsCore.Names.BAT,
                FAIRY = WingsCore.Names.FAIRY,
                EVIL = WingsCore.Names.EVIL,
                DRAGON = WingsCore.Names.DRAGON,
                LVJIA_SUPER = WingsCore.Names.LVJIA_SUPER;

        private Names() {
        }
    }
}
