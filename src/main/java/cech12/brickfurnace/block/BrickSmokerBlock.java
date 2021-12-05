package cech12.brickfurnace.block;

import cech12.brickfurnace.api.tileentity.BrickFurnaceTileEntities;
import cech12.brickfurnace.tileentity.AbstractBrickFurnaceTileEntity;
import cech12.brickfurnace.tileentity.BrickSmokerTileEntity;
import net.minecraft.world.level.block.SmokerBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.MenuProvider;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BrickSmokerBlock extends SmokerBlock {

    public BrickSmokerBlock(BlockBehaviour.Properties builder) {
        super(builder);
    }

    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos blockPos, @Nonnull BlockState blockState) {
        return new BrickSmokerTileEntity(blockPos, blockState);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> entityType) {
        return createTickerHelper(entityType, (BlockEntityType<AbstractBrickFurnaceTileEntity>) BrickFurnaceTileEntities.BRICK_SMOKER, AbstractBrickFurnaceTileEntity::tick);
    }

    /**
     * Interface for handling interaction with blocks that implement AbstractFurnaceBlock. Called in onBlockActivated
     * inside AbstractFurnaceBlock.
     */
    @Override
    protected void openContainer(Level worldIn, @Nonnull BlockPos pos, @Nonnull Player player) {
        BlockEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof BrickSmokerTileEntity) {
            player.openMenu((MenuProvider)tileentity);
            player.awardStat(Stats.INTERACT_WITH_SMOKER);
        }

    }
}
