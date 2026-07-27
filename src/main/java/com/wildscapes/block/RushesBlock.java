package com.wildscapes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rushes are a two-block-tall swamp plant. They grow on the usual plant soils as
 * well as mud, so they feel at home around water.
 */
public class RushesBlock extends DoublePlantBlock {
    public RushesBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT)
                || state.is(Blocks.FARMLAND)
                || state.is(Blocks.MUD)
                || state.is(Blocks.CLAY)
                || state.is(Blocks.SAND);
    }
}
