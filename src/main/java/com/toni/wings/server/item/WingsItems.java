package com.toni.wings.server.item;

import com.toni.wings.WingsMod;
import com.toni.wings.server.apparatus.FlightApparatus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@EventBusSubscriber(modid = WingsMod.ID)
public final class WingsItems {
    private WingsItems() {
    }

    public static final DeferredRegister<Item> REG = DeferredRegister.create(Registries.ITEM, WingsMod.ID);

    public static final DeferredHolder<Item, Item> BAT_BLOOD_BOTTLE = REG.register("bat_blood_bottle",
            () -> new BatBloodBottleItem(properties("bat_blood_bottle")
                    .craftRemainder(Items.GLASS_BOTTLE)
                        .usingConvertsTo(Items.GLASS_BOTTLE)
                    .stacksTo(16)));

    public static final DeferredHolder<Item, Item> ANGEL_WINGS_BOTTLE = REG.register("angel_wings_bottle",
            bottle("angel_wings_bottle", () -> WingsMod.ANGEL_WINGS));
    public static final DeferredHolder<Item, Item> PARROT_WINGS_BOTTLE = REG.register("parrot_wings_bottle",
            bottle("parrot_wings_bottle", () -> WingsMod.PARROT_WINGS));
    public static final DeferredHolder<Item, Item> SLIME_WINGS_BOTTLE = REG.register("slime_wings_bottle",
            bottle("slime_wings_bottle", () -> WingsMod.SLIME_WINGS));
    public static final DeferredHolder<Item, Item> BLUE_BUTTERFLY_WINGS_BOTTLE = REG.register("blue_butterfly_wings_bottle",
            bottle("blue_butterfly_wings_bottle", () -> WingsMod.BLUE_BUTTERFLY_WINGS));
    public static final DeferredHolder<Item, Item> MONARCH_BUTTERFLY_WINGS_BOTTLE = REG.register(
            "monarch_butterfly_wings_bottle",
            bottle("monarch_butterfly_wings_bottle", () -> WingsMod.MONARCH_BUTTERFLY_WINGS));
    public static final DeferredHolder<Item, Item> FIRE_WINGS_BOTTLE = REG.register("fire_wings_bottle",
            bottle("fire_wings_bottle", () -> WingsMod.FIRE_WINGS));
    public static final DeferredHolder<Item, Item> BAT_WINGS_BOTTLE = REG.register("bat_wings_bottle",
            bottle("bat_wings_bottle", () -> WingsMod.BAT_WINGS));
    public static final DeferredHolder<Item, Item> FAIRY_WINGS_BOTTLE = REG.register("fairy_wings_bottle",
            bottle("fairy_wings_bottle", () -> WingsMod.FAIRY_WINGS));
    public static final DeferredHolder<Item, Item> EVIL_WINGS_BOTTLE = REG.register("evil_wings_bottle",
            bottle("evil_wings_bottle", () -> WingsMod.EVIL_WINGS));
    public static final DeferredHolder<Item, Item> DRAGON_WINGS_BOTTLE = REG.register("dragon_wings_bottle",
            bottle("dragon_wings_bottle", () -> WingsMod.DRAGON_WINGS));
    public static final DeferredHolder<Item, Item> LVJIA_SUPER_WINGS_BOTTLE = REG.register("lvjia_super_wings_bottle",
            bottle("lvjia_super_wings_bottle", () -> WingsMod.LVJIA_SUPER_WINGS));

    private static Item.Properties properties(String name) {
        return new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, WingsMod.locate(name)))
                .component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK);
    }

    private static Supplier<Item> bottle(String name, Supplier<FlightApparatus> wings) {
        return () -> new WingsBottleItem(properties(name)
                .craftRemainder(Items.GLASS_BOTTLE)
                        .usingConvertsTo(Items.GLASS_BOTTLE)
                .stacksTo(16), wings.get());
    }

    public static void buildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> tabKey = event.getTabKey();
                if (tabKey == CreativeModeTabs.FOOD_AND_DRINKS) {
                        event.accept(BAT_BLOOD_BOTTLE.get());
                        event.accept(ANGEL_WINGS_BOTTLE.get());
                        event.accept(PARROT_WINGS_BOTTLE.get());
                        event.accept(SLIME_WINGS_BOTTLE.get());
                        event.accept(BLUE_BUTTERFLY_WINGS_BOTTLE.get());
                        event.accept(MONARCH_BUTTERFLY_WINGS_BOTTLE.get());
                        event.accept(FIRE_WINGS_BOTTLE.get());
                        event.accept(BAT_WINGS_BOTTLE.get());
                        event.accept(FAIRY_WINGS_BOTTLE.get());
                        event.accept(EVIL_WINGS_BOTTLE.get());
                        event.accept(DRAGON_WINGS_BOTTLE.get());
                        event.accept(LVJIA_SUPER_WINGS_BOTTLE.get());
                }
                if (tabKey == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                        event.accept(ANGEL_WINGS_BOTTLE.get());
                        event.accept(PARROT_WINGS_BOTTLE.get());
                        event.accept(SLIME_WINGS_BOTTLE.get());
                        event.accept(BLUE_BUTTERFLY_WINGS_BOTTLE.get());
                        event.accept(MONARCH_BUTTERFLY_WINGS_BOTTLE.get());
                        event.accept(FIRE_WINGS_BOTTLE.get());
                        event.accept(BAT_WINGS_BOTTLE.get());
                        event.accept(FAIRY_WINGS_BOTTLE.get());
                        event.accept(EVIL_WINGS_BOTTLE.get());
                        event.accept(DRAGON_WINGS_BOTTLE.get());
                        event.accept(LVJIA_SUPER_WINGS_BOTTLE.get());
                }
    }

}
