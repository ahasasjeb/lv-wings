package cc.lvjia.wings.server.item;

import cc.lvjia.wings.WingsMod;
import cc.lvjia.wings.server.apparatus.FlightApparatus;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;

import java.util.function.Supplier;

public final class WingsItems {
    public static final Supplier<Item> BAT_BLOOD_BOTTLE = register("bat_blood_bottle",
            () -> new BatBloodBottleItem(properties("bat_blood_bottle")
                    .craftRemainder(Items.GLASS_BOTTLE)
                    .usingConvertsTo(Items.GLASS_BOTTLE)
                    .stacksTo(16)));
    public static final Supplier<Item> ANGEL_WINGS_BOTTLE = register("angel_wings_bottle",
            bottle("angel_wings_bottle", () -> WingsMod.ANGEL_WINGS));
    public static final Supplier<Item> PARROT_WINGS_BOTTLE = register("parrot_wings_bottle",
            bottle("parrot_wings_bottle", () -> WingsMod.PARROT_WINGS));
    public static final Supplier<Item> SLIME_WINGS_BOTTLE = register("slime_wings_bottle",
            bottle("slime_wings_bottle", () -> WingsMod.SLIME_WINGS));
    public static final Supplier<Item> BLUE_BUTTERFLY_WINGS_BOTTLE = register("blue_butterfly_wings_bottle",
            bottle("blue_butterfly_wings_bottle", () -> WingsMod.BLUE_BUTTERFLY_WINGS));
    public static final Supplier<Item> MONARCH_BUTTERFLY_WINGS_BOTTLE = register(
            "monarch_butterfly_wings_bottle",
            bottle("monarch_butterfly_wings_bottle", () -> WingsMod.MONARCH_BUTTERFLY_WINGS));
    public static final Supplier<Item> FIRE_WINGS_BOTTLE = register("fire_wings_bottle",
            bottle("fire_wings_bottle", () -> WingsMod.FIRE_WINGS));
    public static final Supplier<Item> BAT_WINGS_BOTTLE = register("bat_wings_bottle",
            bottle("bat_wings_bottle", () -> WingsMod.BAT_WINGS));
    public static final Supplier<Item> FAIRY_WINGS_BOTTLE = register("fairy_wings_bottle",
            bottle("fairy_wings_bottle", () -> WingsMod.FAIRY_WINGS));
    public static final Supplier<Item> EVIL_WINGS_BOTTLE = register("evil_wings_bottle",
            bottle("evil_wings_bottle", () -> WingsMod.EVIL_WINGS));
    public static final Supplier<Item> DRAGON_WINGS_BOTTLE = register("dragon_wings_bottle",
            bottle("dragon_wings_bottle", () -> WingsMod.DRAGON_WINGS));
    public static final Supplier<Item> LVJIA_SUPER_WINGS_BOTTLE = register("lvjia_super_wings_bottle",
            bottle("lvjia_super_wings_bottle", () -> WingsMod.LVJIA_SUPER_WINGS));
    private WingsItems() {
    }

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

    private static Supplier<Item> register(String name, Supplier<Item> factory) {
        Item item = Registry.register(BuiltInRegistries.ITEM, WingsMod.locate(name), factory.get());
        return () -> item;
    }

    public static void register() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> {
            entries.accept(BAT_BLOOD_BOTTLE.get());
            addWings(entries);
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(WingsItems::addWings);
    }

    private static void addWings(FabricCreativeModeTabOutput entries) {
        entries.accept(ANGEL_WINGS_BOTTLE.get());
        entries.accept(PARROT_WINGS_BOTTLE.get());
        entries.accept(SLIME_WINGS_BOTTLE.get());
        entries.accept(BLUE_BUTTERFLY_WINGS_BOTTLE.get());
        entries.accept(MONARCH_BUTTERFLY_WINGS_BOTTLE.get());
        entries.accept(FIRE_WINGS_BOTTLE.get());
        entries.accept(BAT_WINGS_BOTTLE.get());
        entries.accept(FAIRY_WINGS_BOTTLE.get());
        entries.accept(EVIL_WINGS_BOTTLE.get());
        entries.accept(DRAGON_WINGS_BOTTLE.get());
        entries.accept(LVJIA_SUPER_WINGS_BOTTLE.get());
    }

}
