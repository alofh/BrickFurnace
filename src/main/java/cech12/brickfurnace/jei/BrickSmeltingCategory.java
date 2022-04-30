package cech12.brickfurnace.jei;

import cech12.brickfurnace.api.block.BrickFurnaceBlocks;
import cech12.brickfurnace.api.crafting.RecipeTypes;
import cech12.brickfurnace.config.ServerConfig;
import cech12.brickfurnace.crafting.BrickSmeltingRecipe;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.plugins.vanilla.cooking.AbstractCookingCategory;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class BrickSmeltingCategory extends AbstractCookingCategory<BrickSmeltingRecipe> {

    public BrickSmeltingCategory(IGuiHelper guiHelper) {
        super(guiHelper, BrickFurnaceBlocks.BRICK_FURNACE, "gui.jei.category.smelting", (int) (200 * ServerConfig.COOK_TIME_FACTOR.get()));
    }

    @Override
    @Nonnull
    public RecipeType<BrickSmeltingRecipe> getRecipeType() {
        return new RecipeType<>(RecipeTypes.SMELTING_ID, BrickSmeltingRecipe.class);
    }

    @Override
    @Nonnull
    @Deprecated //is deprecated since 9.5.0, getRecipeType() should be used instead
    public ResourceLocation getUid() {
        return RecipeTypes.SMELTING_ID;
    }

    @Override
    @Nonnull
    @Deprecated //is deprecated since 9.5.0, getRecipeType() should be used instead
    public Class<? extends BrickSmeltingRecipe> getRecipeClass() {
        return BrickSmeltingRecipe.class;
    }
}
