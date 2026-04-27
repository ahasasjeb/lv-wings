package com.toni.wings.server.potion;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipe;

import javax.annotation.Nonnull;
import java.util.Objects;

public class PotionMix extends BrewingRecipe {
    private final Potion from;

    public PotionMix(@Nonnull Potion from, @Nonnull Ingredient ingredient, @Nonnull Potion to) {
        this(from, ingredient, createPotionStack(to));
    }

    public PotionMix(@Nonnull Potion from, @Nonnull Ingredient ingredient, @Nonnull ItemStack result) {
        super(Ingredient.of(createPotionStack(from)), ingredient, result);
        this.from = from;
    }

    @Override
    public boolean isInput(@Nonnull ItemStack stack) {
        return !stack.isEmpty() && PotionUtils.getPotion(stack) == this.from;
    }

    @Nonnull
    private static ItemStack createPotionStack(@Nonnull Potion potion) {
        return PotionUtils.setPotion(
            new ItemStack(Objects.requireNonNull(Items.POTION, "Potion item cannot be null")),
            potion
        );
    }
}
