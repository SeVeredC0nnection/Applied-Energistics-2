package appeng.recipes.handlers;

import com.google.gson.JsonObject;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
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
    public Codec<ChargerRecipe> codec() {
        return ChargerRecipe.CODEC;
    }

    @Override
    public ChargerRecipe fromNetwork(FriendlyByteBuf buffer) {
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
