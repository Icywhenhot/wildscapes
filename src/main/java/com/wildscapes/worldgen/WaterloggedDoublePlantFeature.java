package com.wildscapes.worldgen;


import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.core.Holder;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluids;

public record WaterloggedDoublePlantFeature(Holder<BlockStateProvider> toPlace) implements Feature {
    public static final MapCodec<WaterloggedDoublePlantFeature> CODEC = BlockStateProvider.CODEC.fieldOf("to_place")
            .xmap(WaterloggedDoublePlantFeature::new, WaterloggedDoublePlantFeature::toPlace);

    @Override
    public MapCodec<WaterloggedDoublePlantFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos pos) {
        BlockState state = toPlace.value().getOptionalState(level, random, pos);

        if (state == null || !(state.getBlock() instanceof DoublePlantBlock) || !state.canSurvive(level, pos)) {
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
