package com.toni.wings;

import com.mojang.serialization.Lifecycle;
import com.toni.wings.client.ClientProxy;
import com.toni.wings.server.ServerProxy;
import com.toni.wings.server.apparatus.BuffedFlightApparatus;
import com.toni.wings.server.apparatus.FlightApparatus;
import com.toni.wings.server.apparatus.SimpleFlightApparatus;
import com.toni.wings.server.config.WingsConfig;
import com.toni.wings.server.config.WingsItemsConfig;
import com.toni.wings.server.config.WingsOreConfig;
import com.toni.wings.server.effect.WingsEffects;
import com.toni.wings.server.flight.Flight;
import com.toni.wings.server.item.WingsItems;
import com.toni.wings.server.sound.WingsSounds;
import com.toni.wings.util.Util;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Objects;

@Mod(WingsMod.ID)
public final class WingsMod {
    public static final String ID = "wings";

    private static WingsMod INSTANCE;

    public static final Registry<FlightApparatus> WINGS = new DefaultedMappedRegistry<>(
        Util.requireNonnull(Names.NONE.toString(), "Default key cannot be null"),
        Objects.requireNonNull(ResourceKey.createRegistryKey(locate("wings")), "Registry key cannot be null"),
        Objects.requireNonNull(Lifecycle.experimental(), "Lifecycle cannot be null"),
        false
    );

    // Deferred register for command argument types
    private static final DeferredRegister<net.minecraft.commands.synchronization.ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = 
        DeferredRegister.create(net.minecraft.core.registries.Registries.COMMAND_ARGUMENT_TYPE, Objects.requireNonNull(ID, "Mod id cannot be null"));

    // Register the wings argument type
    public static final RegistryObject<net.minecraft.commands.synchronization.SingletonArgumentInfo<com.toni.wings.server.command.WingsArgument>> WINGS_ARGUMENT_TYPE = 
        COMMAND_ARGUMENT_TYPES.register("wings", () -> 
            Objects.requireNonNull(net.minecraft.commands.synchronization.ArgumentTypeInfos.registerByClass(
                com.toni.wings.server.command.WingsArgument.class, 
                Objects.requireNonNull(
                    net.minecraft.commands.synchronization.SingletonArgumentInfo.contextFree(com.toni.wings.server.command.WingsArgument::wings),
                    "Wings argument info cannot be null"
                )
            ), "Wings argument type cannot be null")
        );

    public static final FlightApparatus NONE = registerWing(Names.NONE, FlightApparatus.NONE);
    public static final FlightApparatus WINGLESS = registerWing(Names.WINGLESS, FlightApparatus.NONE);
    public static final FlightApparatus ANGEL_WINGS = registerWing(Names.ANGEL, new SimpleFlightApparatus(WingsItemsConfig.ANGEL));
	public static final FlightApparatus PARROT_WINGS = registerWing(Names.PARROT, new SimpleFlightApparatus(WingsItemsConfig.PARROT));
    public static final FlightApparatus BAT_WINGS = registerWing(Names.BAT, new SimpleFlightApparatus(WingsItemsConfig.BAT));
    public static final FlightApparatus BLUE_BUTTERFLY_WINGS = registerWing(Names.BLUE_BUTTERFLY, new SimpleFlightApparatus(WingsItemsConfig.BLUE_BUTTERFLY));
    public static final FlightApparatus DRAGON_WINGS = registerWing(Names.DRAGON, new SimpleFlightApparatus(WingsItemsConfig.DRAGON));
    public static final FlightApparatus EVIL_WINGS = registerWing(Names.EVIL, new SimpleFlightApparatus(WingsItemsConfig.EVIL));
    public static final FlightApparatus FAIRY_WINGS = registerWing(Names.FAIRY, new SimpleFlightApparatus(WingsItemsConfig.FAIRY));
    public static final FlightApparatus MONARCH_BUTTERFLY_WINGS = registerWing(Names.MONARCH_BUTTERFLY, new SimpleFlightApparatus(WingsItemsConfig.MONARCH_BUTTERFLY));
    public static final FlightApparatus SLIME_WINGS = registerWing(Names.SLIME, new SimpleFlightApparatus(WingsItemsConfig.SLIME));
    public static final FlightApparatus FIRE_WINGS = registerWing(Names.FIRE, new SimpleFlightApparatus(WingsItemsConfig.FIRE));
    public static final FlightApparatus LVJIA_SUPER_WINGS = registerWing(Names.LVJIA_SUPER,
        new BuffedFlightApparatus(WingsItemsConfig.LVJIA_SUPER,
            BuffedFlightApparatus.EffectSettings.of(MobEffects.DAMAGE_RESISTANCE, 2, 40, 40),
            BuffedFlightApparatus.EffectSettings.of(MobEffects.JUMP, 1, 40, 40)));
    //public static final FlightApparatus METALLIC_WINGS = Registry.register(WINGS, Names.METALLIC, new SimpleFlightApparatus(WingsItemsConfig.METALLIC));


    private Proxy proxy;

    private static FlightApparatus registerWing(ResourceLocation name, FlightApparatus apparatus) {
        return Registry.register(
            Objects.requireNonNull(WINGS, "Wing registry cannot be null"),
            Objects.requireNonNull(name, "Wing id cannot be null"),
            Objects.requireNonNull(apparatus, "Wing apparatus cannot be null")
        );
    }

    public WingsMod() {
        if (INSTANCE != null) throw new IllegalStateException("Already constructed!");
        INSTANCE = this;
        IEventBus bus = getModEventBus();
    ModLoadingContext context = getModLoadingContext();
    context.registerConfig(ModConfig.Type.COMMON, WingsConfig.SPEC, Objects.requireNonNull(ID + "-common.toml", "Config name cannot be null"));
    context.registerConfig(ModConfig.Type.COMMON, WingsItemsConfig.SPEC, Objects.requireNonNull(ID + "-items.toml", "Config name cannot be null"));
    context.registerConfig(ModConfig.Type.COMMON, WingsOreConfig.SPEC, Objects.requireNonNull(ID + "-ores.toml", "Config name cannot be null"));
    WingsItems.REG.register(bus);
    bus.addListener(WingsItems::buildCreativeTabContents);
        WingsSounds.REG.register(bus);
        WingsEffects.REG.register(bus);
        COMMAND_ARGUMENT_TYPES.register(bus);
        this.proxy = DistExecutor.unsafeRunForDist(() -> () -> new ClientProxy(), () -> () -> new ServerProxy());
        this.proxy.init(bus);
    }

    @SuppressWarnings("removal")
    private static IEventBus getModEventBus() {
        return FMLJavaModLoadingContext.get().getModEventBus();
    }

    @SuppressWarnings("removal")
    private static ModLoadingContext getModLoadingContext() {
        return ModLoadingContext.get();
    }

    public void addFlightListeners(Player player, Flight instance) {
        this.requireProxy().addFlightListeners(player, instance);
    }

    public static WingsMod instance() {
        return INSTANCE;
    }

    private Proxy requireProxy() {
        if (this.proxy == null) {
            throw new IllegalStateException("Proxy not initialized");
        }
        return this.proxy;
    }

    @Nonnull
    public static ResourceLocation locate(@Nonnull String name)
    {
        ResourceLocation location = ResourceLocation.tryBuild(
            Objects.requireNonNull(WingsMod.ID, "Mod id cannot be null"),
            Objects.requireNonNull(name, "Resource path cannot be null")
        );
        if (location == null) {
            throw new IllegalArgumentException("Invalid resource path: " + name);
        }
        return location;
    }

    public static final class Names {
        private Names() {
        }

        public static final ResourceLocation
            NONE = create("none"),
            WINGLESS = create("wingless"),
            ANGEL = create("angel_wings"),
            //METALLIC = create("metallic_wings"),
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

        private static ResourceLocation create(String path) {
            ResourceLocation location = ResourceLocation.tryBuild(
                Objects.requireNonNull(ID, "Mod id cannot be null"),
                Objects.requireNonNull(path, "Resource path cannot be null")
            );
            if (location == null) {
                throw new IllegalArgumentException("Invalid resource path: " + path);
            }
            return location;
        }
    }
}
