package cc.lvjia.wings.server.item;

import net.minecraft.world.item.Item;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class WingsItemCatalog {
    private WingsItemCatalog() {
    }

    public static void forEachWingBottle(Consumer<Supplier<? extends Item>> consumer) {
        consumer.accept(WingsItems.ANGEL_WINGS_BOTTLE);
        consumer.accept(WingsItems.PARROT_WINGS_BOTTLE);
        consumer.accept(WingsItems.SLIME_WINGS_BOTTLE);
        consumer.accept(WingsItems.BLUE_BUTTERFLY_WINGS_BOTTLE);
        consumer.accept(WingsItems.MONARCH_BUTTERFLY_WINGS_BOTTLE);
        consumer.accept(WingsItems.FIRE_WINGS_BOTTLE);
        consumer.accept(WingsItems.BAT_WINGS_BOTTLE);
        consumer.accept(WingsItems.FAIRY_WINGS_BOTTLE);
        consumer.accept(WingsItems.EVIL_WINGS_BOTTLE);
        consumer.accept(WingsItems.DRAGON_WINGS_BOTTLE);
        consumer.accept(WingsItems.LVJIA_SUPER_WINGS_BOTTLE);
    }
}
