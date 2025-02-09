package cech12.brickfurnace.blockentity;

import cech12.brickfurnace.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public abstract class AbstractBrickFurnaceBlockEntity extends AbstractFurnaceBlockEntity {

    protected final RecipeType<? extends AbstractCookingRecipe> specificRecipeType;
    protected final RecipeType<? extends AbstractCookingRecipe> vanillaRecipeType;

    public AbstractBrickFurnaceBlockEntity(BlockEntityType<?> blockEntityTypeIn,
                                           BlockPos blockPos,
                                           BlockState blockState,
                                           RecipeType<? extends AbstractCookingRecipe> specificRecipeTypeIn,
                                           RecipeType<? extends AbstractCookingRecipe> vanillaRecipeTypeIn) {
        super(blockEntityTypeIn, blockPos, blockState, vanillaRecipeTypeIn);
        this.specificRecipeType = specificRecipeTypeIn;
        this.vanillaRecipeType = vanillaRecipeTypeIn;
    }

    /* FOLLOWING Code helps the copied code below. */

    public static final int BURN_TIME = 0;
    public static final int RECIPES_USED = 1;
    public static final int COOK_TIME = 2;
    public static final int COOK_TIME_TOTAL = 3;

    /* FOLLOWING Code is copied from "Shadows-of-Fire/FastFurnace" mod to enhance performance */

    public static final int INPUT = 0;
    public static final int FUEL = 1;
    public static final int OUTPUT = 2;

    protected AbstractCookingRecipe curRecipe;
    protected ItemStack failedMatch = ItemStack.EMPTY;

    private boolean isBurning() {
        return this.dataAccess.get(BURN_TIME) > 0; //changed because of private variable
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AbstractBrickFurnaceBlockEntity entity) {
        boolean wasBurning = entity.isBurning();
        boolean dirty = false;
        if (entity.isBurning()) {
            entity.dataAccess.set(BURN_TIME, entity.dataAccess.get(BURN_TIME) - 1); //changed because of private variable
        }

        if (level != null && !level.isClientSide) {
            ItemStack fuel = entity.items.get(FUEL);
            if (entity.isBurning() || !fuel.isEmpty() && !entity.items.get(INPUT).isEmpty()) {
                AbstractCookingRecipe irecipe = entity.getRecipe();
                boolean valid = entity.canBurn(irecipe);
                if (!entity.isBurning() && valid) {
                    entity.dataAccess.set(BURN_TIME, entity.getBurnDuration(fuel)); //changed because of private variable
                    entity.dataAccess.set(RECIPES_USED, entity.dataAccess.get(BURN_TIME)); //changed because of private variable
                    if (entity.isBurning()) {
                        dirty = true;
                        if (fuel.hasCraftingRemainingItem()) entity.items.set(1, fuel.getCraftingRemainingItem());
                        else if (!fuel.isEmpty()) {
                            fuel.shrink(1);
                            if (fuel.isEmpty()) {
                                entity.items.set(1, fuel.getCraftingRemainingItem());
                            }
                        }
                    }
                }

                if (entity.isBurning() && valid) {
                    entity.dataAccess.set(COOK_TIME, entity.dataAccess.get(COOK_TIME) + 1); //changed because of private variable
                    if (entity.dataAccess.get(COOK_TIME) == entity.dataAccess.get(COOK_TIME_TOTAL)) { //changed because of private variable
                        entity.dataAccess.set(COOK_TIME, 0); //changed because of private variable
                        entity.dataAccess.set(COOK_TIME_TOTAL, entity.getTotalCookTime()); //changed because of private variable
                        entity.smeltItem(irecipe);
                        dirty = true;
                    }
                } else {
                    entity.dataAccess.set(COOK_TIME, 0); //changed because of private variable
                }
            } else if (!entity.isBurning() && entity.dataAccess.get(COOK_TIME) > 0) { //changed because of private variable
                entity.dataAccess.set(COOK_TIME, Mth.clamp(entity.dataAccess.get(COOK_TIME) - 2, 0, entity.dataAccess.get(COOK_TIME_TOTAL))); //changed because of private variable
            }

            if (wasBurning != entity.isBurning()) {
                dirty = true;
                level.setBlock(pos, state.setValue(AbstractFurnaceBlock.LIT, entity.isBurning()), 3);
            }
        }

        if (dirty) {
            entity.setChanged();
        }

    }

    private boolean canBurn(@Nullable Recipe<?> recipe) {
        if (!this.items.get(0).isEmpty() && recipe != null) {
            ItemStack recipeOutput = recipe.getResultItem();
            if (!recipeOutput.isEmpty()) {
                ItemStack output = this.items.get(OUTPUT);
                if (output.isEmpty()) return true;
                else if (!output.sameItem(recipeOutput)) return false;
                else return output.getCount() + recipeOutput.getCount() <= output.getMaxStackSize();
            }
        }
        return false;
    }

    private void smeltItem(@Nullable Recipe<?> recipe) {
        if (recipe != null && this.canBurn(recipe)) {
            ItemStack itemstack = this.items.get(0);
            ItemStack itemstack1 = recipe.getResultItem();
            ItemStack itemstack2 = this.items.get(2);
            if (itemstack2.isEmpty()) {
                this.items.set(2, itemstack1.copy());
            } else if (itemstack2.getItem() == itemstack1.getItem()) {
                itemstack2.grow(itemstack1.getCount());
            }

            if (this.level != null && !this.level.isClientSide) {
                this.setRecipeUsed(recipe);
            }

            if (itemstack.getItem() == Blocks.WET_SPONGE.asItem() && !this.items.get(1).isEmpty() && this.items.get(1).getItem() == Items.BUCKET) {
                this.items.set(1, new ItemStack(Items.WATER_BUCKET));
            }

            itemstack.shrink(1);
        }
    }

    private int getTotalCookTime() {
        AbstractCookingRecipe rec = getRecipe();
        if (rec == null) {
            return 200;
        } else if (this.specificRecipeType.getClass().isInstance(rec.getType())) {
            return rec.getCookingTime();
        }
        return (int) (rec.getCookingTime() * ServerConfig.COOK_TIME_FACTOR.get());
    }

    public AbstractCookingRecipe getRecipe() {
        ItemStack input = this.getItem(INPUT);
        if (input.isEmpty() || input == failedMatch) {
            return null;
        }
        if (this.level != null && curRecipe != null && curRecipe.matches(this, level)) {
            return curRecipe;
        } else {
            AbstractCookingRecipe rec = null;
            if (this.level != null) {
                rec = this.level.getRecipeManager().getRecipeFor(this.specificRecipeType, this, this.level).orElse(null);
                if (rec == null && ServerConfig.VANILLA_RECIPES_ENABLED.get()) {
                    rec = this.level.getRecipeManager().getRecipesFor(this.vanillaRecipeType, this, this.level)
                            .stream().filter(abstractCookingRecipe -> ServerConfig.isRecipeNotBlacklisted(abstractCookingRecipe.getId())).findFirst().orElse(null);
                }
            }
            if (rec == null) {
                failedMatch = input;
            } else {
                failedMatch = ItemStack.EMPTY;
            }
            return curRecipe = rec;
        }
    }

    public ContainerData getContainerData() {
        return this.dataAccess;
    }

}
