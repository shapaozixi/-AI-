package cn.autoforged.xiuxian_cultivation_mod_1789994530.datagen;

import cn.autoforged.xiuxian_cultivation_mod_1789994530.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // 炼体丹：铁锭 + 金锭 + 荧石粉 + 地狱疣
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BREWING, ModItems.BODY_TEMPERING_PILL.get(), 1)
                .requires(Items.IRON_INGOT)
                .requires(Items.GOLD_INGOT)
                .requires(Items.GLOWSTONE_DUST)
                .requires(Items.NETHER_WART)
                .unlockedBy("has_nether_wart", has(Items.NETHER_WART))
                .save(recipeOutput);
    }
}
