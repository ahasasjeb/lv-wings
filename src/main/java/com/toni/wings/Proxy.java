package com.toni.wings;

import com.toni.wings.server.config.WingsConfig;
import com.toni.wings.server.config.WingsItemsConfig;
import com.toni.wings.server.config.WingsOreConfig;
import com.toni.wings.server.flight.Flight;
import com.toni.wings.server.item.WingsItems;
import com.toni.wings.server.net.Network;
import com.toni.wings.server.net.clientbound.MessageSyncFlight;
import com.toni.wings.server.potion.PotionMix;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

public abstract class Proxy {
    protected final Network network = new Network();

    public void init(IEventBus modBus) {
        modBus.addListener(this::setup);
        this.network.register(modBus);
        NeoForge.EVENT_BUS.addListener(this::registerBrewingRecipes);
    }

    protected void setup(FMLCommonSetupEvent event) {
        // Validate configs after they are loaded
        WingsConfig.validate();
        WingsItemsConfig.validate();
        WingsOreConfig.validate();
    }

    private void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        var builder = event.getBuilder();
        BiConsumer<ItemLike, Supplier<? extends Item>> reg = (item, supplier) -> {
            builder.addRecipe(new PotionMix(Potions.SLOW_FALLING, Ingredient.of(item), new ItemStack(supplier.get())));
            builder.addRecipe(new PotionMix(Potions.LONG_SLOW_FALLING, Ingredient.of(item), new ItemStack(supplier.get())));
        };

        reg.accept(Items.FEATHER, WingsItems.ANGEL_WINGS_BOTTLE);
        reg.accept(Items.RED_DYE, WingsItems.PARROT_WINGS_BOTTLE);
        reg.accept(WingsItems.BAT_BLOOD_BOTTLE.get(), WingsItems.BAT_WINGS_BOTTLE);
        reg.accept(Items.BLUE_DYE, WingsItems.BLUE_BUTTERFLY_WINGS_BOTTLE);
        reg.accept(Items.LEATHER, WingsItems.DRAGON_WINGS_BOTTLE);
        reg.accept(Items.BONE, WingsItems.EVIL_WINGS_BOTTLE);
        reg.accept(Items.OXEYE_DAISY, WingsItems.FAIRY_WINGS_BOTTLE);
        reg.accept(Items.BLAZE_POWDER, WingsItems.FIRE_WINGS_BOTTLE);
        reg.accept(Items.ORANGE_DYE, WingsItems.MONARCH_BUTTERFLY_WINGS_BOTTLE);
        reg.accept(Items.SLIME_BALL, WingsItems.SLIME_WINGS_BOTTLE);
        //reg.accept(Items.IRON_INGOT, WingsItems.METALLIC_WINGS_BOTTLE);
    }

    @SuppressWarnings("deprecation")
    public void addFlightListeners(Player player, Flight instance) {
        if (player instanceof ServerPlayer) {
            instance.registerFlyingListener(isFlying -> player.getAbilities().mayfly = isFlying);
            instance.registerFlyingListener(isFlying -> {
                if (isFlying) {
                    player.removeVehicle();
                }
            });
            Flight.Notifier notifier = Flight.Notifier.of(
                () -> this.network.sendToPlayer(new MessageSyncFlight(player, instance), (ServerPlayer) player),
                p -> this.network.sendToPlayer(new MessageSyncFlight(player, instance), p),
                () -> this.network.sendToAllTracking(new MessageSyncFlight(player, instance), player)
            );
            instance.registerSyncListener(players -> players.notify(notifier));
        }
    }
}
