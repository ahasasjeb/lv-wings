import cc.lvjia.wings.client.model.ModelWingsAvian;
import cc.lvjia.wings.client.model.ModelWingsInsectoid;
import cc.lvjia.wings.server.item.WingsItems;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.BrewingInput;
import net.minecraft.world.item.crafting.BrewingRecipe;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MigrationProbe {
    public static void verify() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        WingsItems.register();
        new ModelWingsAvian(ModelWingsAvian.createBodyLayer().bakeRoot());
        new ModelWingsInsectoid(ModelWingsInsectoid.createBodyLayer().bakeRoot());
        System.out.println("WING_MODELS_OK");
        var lookup = net.minecraft.data.registries.VanillaRegistries.createWorldLookup();
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(lookup).forEach(pending -> pending.apply());
        var ops = RegistryOps.create(JsonOps.INSTANCE, lookup);
        try (var files = Files.list(Path.of(System.getProperty("wings.recipeDir")))) {
            int count = 0;
            for (var path : files.filter(p -> p.toString().endsWith(".json")).toList()) {
                var json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                var recipe = BrewingRecipe.MAP_CODEC.codec().parse(ops, json).getOrThrow();
                var ingredientId = Identifier.parse(json.getAsJsonObject("reagent").get("item").getAsString());
                var reagent = new ItemStack(BuiltInRegistries.ITEM.getValue(ingredientId));
                for (var potion : java.util.List.of(Potions.SLOW_FALLING, Potions.LONG_SLOW_FALLING)) {
                    var input = new BrewingInput(PotionContents.createItemStack(Items.POTION, potion), reagent);
                    if (!recipe.matches(input)) throw new AssertionError("Recipe rejected " + path + " / " + potion);
                    var expected = Identifier.parse(json.getAsJsonObject("output").get("id").getAsString());
                    if (!BuiltInRegistries.ITEM.getKey(recipe.assemble(input).getItem()).equals(expected)) {
                        throw new AssertionError("Wrong result: " + path);
                    }
                }
                if (recipe.matches(new BrewingInput(PotionContents.createItemStack(Items.POTION, Potions.WATER), reagent))
                        || recipe.matches(new BrewingInput(PotionContents.createItemStack(Items.SPLASH_POTION, Potions.SLOW_FALLING), reagent))
                        || recipe.matches(new BrewingInput(PotionContents.createItemStack(Items.POTION, Potions.SLOW_FALLING), new ItemStack(Items.DIRT)))) {
                    throw new AssertionError("Recipe accepted wrong input: " + path);
                }
                count++;
            }
            if (count != 10) throw new AssertionError("Expected ten recipes, got " + count);
            System.out.println("BREWING_RECIPES_OK 10 recipes, 20 valid combinations, 30 rejected combinations");
        }
    }
}
