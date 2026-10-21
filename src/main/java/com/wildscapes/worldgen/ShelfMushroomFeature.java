package com.wildscapes.worldgen;

import com.mojang.serialization.Codec;
import com.wildscapes.block.ShelfMushroomBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.Heightmap;

public class ShelfMushroomFeature extends Feature<SimpleBlockConfiguration> {
    public ShelfMushroomFeature(Codec<SimpleBlockConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SimpleBlockConfiguration> context) {
        var level = context.level();
        var random = context.random();
        BlockState mushroom = context.config().toPlace().getState(random, context.origin());
        for (int i = 0; i < 24; i++) {
            int x = context.origin().getX() + random.nextInt(7) - 3;
            int z = context.origin().getZ() + random.nextInt(7) - 3;
            BlockPos ground = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1, z);
            while (ground.getY() > level.getMinBuildHeight()
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
