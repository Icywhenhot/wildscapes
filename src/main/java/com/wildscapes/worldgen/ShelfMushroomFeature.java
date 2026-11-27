package com.wildscapes.worldgen;

import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.core.Holder;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.WorldGenLevel;
import com.wildscapes.block.ShelfMushroomBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.Heightmap;

public record ShelfMushroomFeature(Holder<BlockStateProvider> toPlace) implements Feature {
    public static final MapCodec<ShelfMushroomFeature> CODEC = BlockStateProvider.CODEC.fieldOf("to_place")
            .xmap(ShelfMushroomFeature::new, ShelfMushroomFeature::toPlace);

    @Override
    public MapCodec<ShelfMushroomFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        BlockState mushroom = toPlace.value().getOptionalState(level, random, origin);
        if (mushroom == null) {
            return false;
        }
        for (int i = 0; i < 24; i++) {
            int x = origin.getX() + random.nextInt(7) - 3;
            int z = origin.getZ() + random.nextInt(7) - 3;
            BlockPos ground = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1, z);
            while (ground.getY() > level.getMinY()
                    && (level.getBlockState(ground).is(BlockTags.LOGS) || level.getBlockState(ground).is(BlockTags.LEAVES))) {
                ground = ground.below();
            }
            BlockPos pos = ground.above(1 + Math.min(random.nextInt(4), random.nextInt(4)));
            if (!level.isEmptyBlock(pos) || !level.getBiome(pos).is(Biomes.SWAMP)) continue;
            for (Direction face : Direction.Plane.HORIZONTAL) {
                BlockState state = mushroom.setValue(ShelfMushroomBlock.FACING, face);
                if (state.canSurvive(level, pos)) {
                    level.setBlock(pos, state, 2);
                    return true;
                }
            }
        }
        return false;
    }
}
