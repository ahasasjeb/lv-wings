package cc.lvjia.wings.server.potion;

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
import java.util.stream.Stream;

@SuppressWarnings("null")
public final class WingsBrewingRecipes {
    private static final List<Mix> MIXES = createMixes();

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
        return potion.filter(holder -> holder.is(Potions.SLOW_FALLING.key()) || holder.is(Potions.LONG_SLOW_FALLING.key()))
                .isPresent();
    }

    private static Mix mix(ItemLike ingredient, Supplier<? extends Item> result) {
        return new Mix(Ingredient.of(ingredient), result);
    }

    private static List<Mix> createMixes() {
        Stream.Builder<Mix> builder = Stream.builder();
        WingsBrewingCatalog.forEachMix((ingredient, result) -> builder.add(mix(ingredient, result)));
        return builder.build().toList();
    }

    private record Mix(Ingredient ingredient, Supplier<? extends Item> result) {
    }
}
