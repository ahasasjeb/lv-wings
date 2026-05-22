package com.toni.wings;

import com.toni.wings.server.config.WingsConfig;
import com.toni.wings.server.config.WingsItemsConfig;
import com.toni.wings.server.dreamcatcher.InSomniable;
import com.toni.wings.server.flight.Flight;
import com.toni.wings.server.flight.FlightAbilities;
import com.toni.wings.server.item.WingsItems;
import com.toni.wings.server.net.Network;
import com.toni.wings.server.net.clientbound.MessageSyncFlight;
import com.toni.wings.server.potion.PotionMix;
import com.toni.wings.util.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class Proxy {
    protected final Network network = new Network();

    public void init(IEventBus modBus) {
        modBus.addListener(this::setup);
        modBus.addListener(this::registerCapabilities);
    }

    protected void setup(FMLCommonSetupEvent event) {
        // Validate configs after they are loaded
        WingsConfig.validate();
        WingsItemsConfig.validate();

        event.enqueueWork(() -> {
            record BottleRecipe(ItemLike ingredient, RegistryObject<Item> result) {
            }
            BiConsumer<ItemLike, RegistryObject<Item>> reg = (item, obj) -> {
                ItemLike recipeItem = Util.requireNonnull(item, "酿造材料不能为空");
                Ingredient ingredient = Util.requireNonnull(Ingredient.of(recipeItem), "酿造配方材料不能为空");
                ItemStack potionResult = Util.requireNonnull(
                    Util.requireNonnull(obj.get(), "翅膀药剂物品不能为空").getDefaultInstance(),
                    "翅膀药剂物品默认堆叠不能为空"
                );
                BrewingRecipeRegistry.addRecipe(
                    new PotionMix(requirePotion(Potions.SLOW_FALLING), ingredient, Util.requireNonnull(potionResult.copy(), "药剂结果不能为空"))
                );
                BrewingRecipeRegistry.addRecipe(
                    new PotionMix(requirePotion(Potions.LONG_SLOW_FALLING), ingredient, Util.requireNonnull(potionResult.copy(), "长效药剂结果不能为空"))
                );
            };
            List.of(
                new BottleRecipe(Items.FEATHER, WingsItems.ANGEL_WINGS_BOTTLE),
                new BottleRecipe(Items.RED_DYE, WingsItems.PARROT_WINGS_BOTTLE),
                new BottleRecipe(WingsItems.BAT_BLOOD_BOTTLE.get(), WingsItems.BAT_WINGS_BOTTLE),
                new BottleRecipe(Items.BLUE_DYE, WingsItems.BLUE_BUTTERFLY_WINGS_BOTTLE),
                new BottleRecipe(Items.LEATHER, WingsItems.DRAGON_WINGS_BOTTLE),
                new BottleRecipe(Items.BONE, WingsItems.EVIL_WINGS_BOTTLE),
                new BottleRecipe(Items.OXEYE_DAISY, WingsItems.FAIRY_WINGS_BOTTLE),
                new BottleRecipe(Items.BLAZE_POWDER, WingsItems.FIRE_WINGS_BOTTLE),
                new BottleRecipe(Items.ORANGE_DYE, WingsItems.MONARCH_BUTTERFLY_WINGS_BOTTLE),
                new BottleRecipe(Items.SLIME_BALL, WingsItems.SLIME_WINGS_BOTTLE)
            ).forEach(recipe -> reg.accept(recipe.ingredient(), recipe.result()));
        });
    }

    protected void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.register(Flight.class);
        event.register(InSomniable.class);
    }

    public void addFlightListeners(Player player, Flight instance) {
        if (player instanceof ServerPlayer) {
            instance.registerFlyingListener(isFlying -> FlightAbilities.updateForModFlight(player, isFlying));
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

    @Nonnull
    private static Potion requirePotion(Potion potion) {
        return Util.requireNonnull(potion, "基础药水不能为空");
    }
}
