package appeng.recipes.handlers;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class ChargerRecipeSerializer implements RecipeSerializer<ChargerRecipe> {

    public static final ChargerRecipeSerializer INSTANCE = new ChargerRecipeSerializer();

    @Override
    public ChargerRecipe fromJson(ResourceLocation recipeId, JsonObject serializedRecipe) {

        Ingredient ingredient = Ingredient.fromJson(serializedRecipe.get("ingredient"));
        Item result = ShapedRecipe.itemFromJson(GsonHelper.getAsJsonObject(serializedRecipe, "result"));

        return new ChargerRecipe(ingredient, result);
    }

    @Override
    public ChargerRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        Ingredient ingredient = Ingredient.fromNetwork(buffer);
        ItemStack result = buffer.readItem();

        return new ChargerRecipe(ingredient, result.getItem());
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, ChargerRecipe recipe) {
        recipe.ingredient.toNetwork(buffer);
        buffer.writeItem(new ItemStack(recipe.result));
    }
}
