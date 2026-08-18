package com.wildscapes.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The knee-high rush: a single block of the same plant as {@link RushesBlock}, caught before it
 * has run to full height. It takes the same soils as its taller sibling, but not the water —
 * worldgen grows it along the bank while the tall rushes stand in the shallows.
 */
public class ShortRushesBlock extends BushBlock {
    public static final MapCodec<ShortRushesBlock> CODEC = simpleCodec(ShortRushesBlock::new);

    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

    public ShortRushesBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return RushesBlock.isRushSoil(state);
    }
}
