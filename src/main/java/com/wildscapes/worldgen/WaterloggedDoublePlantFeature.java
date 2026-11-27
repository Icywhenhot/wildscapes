package com.wildscapes.worldgen;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.material.Fluids;

public class WaterloggedDoublePlantFeature extends Feature<SimpleBlockConfiguration> {
    public WaterloggedDoublePlantFeature(Codec<SimpleBlockConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SimpleBlockConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        BlockState state = context.config().toPlace().getState(context.random(), pos);

        if (!(state.getBlock() instanceof DoublePlantBlock) || !state.canSurvive(level, pos)) {
            return false;
        }
        if (!canGrowInto(level.getBlockState(pos)) || !canGrowInto(level.getBlockState(pos.above()))) {
            return false;
        }

        DoublePlantBlock.placeAt(level, state, pos, 2);
        return true;
    }

    private static boolean canGrowInto(BlockState state) {
        return state.isAir() || state.getFluidState().isSourceOfType(Fluids.WATER);
    }
}
