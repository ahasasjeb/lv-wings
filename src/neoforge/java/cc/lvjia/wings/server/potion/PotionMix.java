package cc.lvjia.wings.server.potion;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;

public class PotionMix extends BrewingRecipe {
    private final Holder<Potion> from;
    private final ResourceKey<Potion> fromKey;

    public PotionMix(Holder<Potion> from, Ingredient ingredient, Holder<Potion> to) {
        this(from, ingredient, createPotionStack(to));
    }

    public PotionMix(Holder<Potion> from, Ingredient ingredient, ItemStack result) {
        super(Ingredient.of(Items.POTION), ingredient, result);
        this.from = from;
        this.fromKey = from.unwrapKey().orElse(null);
    }

    private static ItemStack createPotionStack(Holder<Potion> potion) {
        return PotionContents.createItemStack(Items.POTION, potion);
    }

    @Override
    public boolean isInput(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return contents.potion().map(this::matchesFrom).orElse(false);
    }

    private boolean matchesFrom(Holder<Potion> candidate) {
        if (this.fromKey != null) {
            return candidate.is(this.fromKey);
        }

        return candidate.unwrapKey()
                .map(this.from::is)
                .orElse(candidate.value() == this.from.value());
    }
}
