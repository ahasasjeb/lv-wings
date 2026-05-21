package cc.lvjia.wings.server.potion;

import cc.lvjia.wings.server.item.WingsItems;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class WingsBrewingRecipes {
    private static final List<Mix> MIXES = List.of(
            mix(Items.FEATHER, WingsItems.ANGEL_WINGS_BOTTLE),
            mix(Items.RED_DYE, WingsItems.PARROT_WINGS_BOTTLE),
            mix(WingsItems.BAT_BLOOD_BOTTLE.get(), WingsItems.BAT_WINGS_BOTTLE),
            mix(Items.BLUE_DYE, WingsItems.BLUE_BUTTERFLY_WINGS_BOTTLE),
            mix(Items.LEATHER, WingsItems.DRAGON_WINGS_BOTTLE),
            mix(Items.BONE, WingsItems.EVIL_WINGS_BOTTLE),
            mix(Items.OXEYE_DAISY, WingsItems.FAIRY_WINGS_BOTTLE),
            mix(Items.BLAZE_POWDER, WingsItems.FIRE_WINGS_BOTTLE),
            mix(Items.ORANGE_DYE, WingsItems.MONARCH_BUTTERFLY_WINGS_BOTTLE),
            mix(Items.SLIME_BALL, WingsItems.SLIME_WINGS_BOTTLE));

    private WingsBrewingRecipes() {
    }

    public static boolean isIngredient(ItemStack ingredient) {
        return MIXES.stream().anyMatch(mix -> mix.ingredient().test(ingredient));
    }

    public static boolean hasMix(ItemStack source, ItemStack ingredient) {
        return isSlowFalling(source) && isIngredient(ingredient);
    }

    public static ItemStack mix(ItemStack ingredient, ItemStack source) {
        if (!isSlowFalling(source)) {
            return ItemStack.EMPTY;
        }
        return MIXES.stream()
                .filter(mix -> mix.ingredient().test(ingredient))
                .findFirst()
                .map(mix -> new ItemStack(mix.result().get()))
                .orElse(ItemStack.EMPTY);
    }

    private static boolean isSlowFalling(ItemStack source) {
        if (!source.is(Items.POTION)) {
            return false;
        }
        Optional<Holder<Potion>> potion = source.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                .potion();
        return potion.filter(holder -> holder.is(Potions.SLOW_FALLING) || holder.is(Potions.LONG_SLOW_FALLING))
                .isPresent();
    }

    private static Mix mix(ItemLike ingredient, Supplier<? extends Item> result) {
        return new Mix(Ingredient.of(ingredient), result);
    }

    private record Mix(Ingredient ingredient, Supplier<? extends Item> result) {
    }
}
