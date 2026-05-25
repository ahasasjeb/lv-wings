package cc.lvjia.wings.server.potion;

import cc.lvjia.wings.server.item.WingsItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class WingsBrewingCatalog {
    private WingsBrewingCatalog() {
    }

    public static void forEachMix(BiConsumer<ItemLike, Supplier<? extends Item>> consumer) {
        consumer.accept(Items.FEATHER, WingsItems.ANGEL_WINGS_BOTTLE);
        consumer.accept(Items.RED_DYE, WingsItems.PARROT_WINGS_BOTTLE);
        consumer.accept(WingsItems.BAT_BLOOD_BOTTLE.get(), WingsItems.BAT_WINGS_BOTTLE);
        consumer.accept(Items.BLUE_DYE, WingsItems.BLUE_BUTTERFLY_WINGS_BOTTLE);
        consumer.accept(Items.LEATHER, WingsItems.DRAGON_WINGS_BOTTLE);
        consumer.accept(Items.BONE, WingsItems.EVIL_WINGS_BOTTLE);
        consumer.accept(Items.OXEYE_DAISY, WingsItems.FAIRY_WINGS_BOTTLE);
        consumer.accept(Items.BLAZE_POWDER, WingsItems.FIRE_WINGS_BOTTLE);
        consumer.accept(Items.ORANGE_DYE, WingsItems.MONARCH_BUTTERFLY_WINGS_BOTTLE);
        consumer.accept(Items.SLIME_BALL, WingsItems.SLIME_WINGS_BOTTLE);
    }
}
