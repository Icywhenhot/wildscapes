package com.wildscapes.block;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LuminousMushroomBlock extends VegetationBlock {

    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 13, 14);

    public LuminousMushroomBlock(Properties properties) {
        super(properties);
    }


    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(WildscapesBlocks.SOIL) || state.is(Blocks.MUD) || state.is(Blocks.CLAY)
                || state.is(Blocks.FARMLAND);
    }
}
