package com.wildscapes.worldgen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class ShoreDensityFilter extends PlacementFilter {
    public static final MapCodec<ShoreDensityFilter> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.intRange(1, 16).fieldOf("max_distance").forGetter(filter -> filter.maxDistance),
                    Codec.BOOL.optionalFieldOf("include_land", false).forGetter(filter -> filter.includeLand))
                    .apply(instance, ShoreDensityFilter::new));

    private final int maxDistance;
    private final boolean includeLand;

    private final int[][] offsets;

    public ShoreDensityFilter(int maxDistance, boolean includeLand) {
        this.maxDistance = maxDistance;
        this.includeLand = includeLand;

        List<int[]> inRange = new ArrayList<>();
        for (int dx = -maxDistance; dx <= maxDistance; dx++) {
            for (int dz = -maxDistance; dz <= maxDistance; dz++) {
                if ((dx != 0 || dz != 0) && dx * dx + dz * dz <= maxDistance * maxDistance) {
                    inRange.add(new int[] {dx, dz});
                }
            }
        }
        inRange.sort(Comparator.comparingInt(offset -> offset[0] * offset[0] + offset[1] * offset[1]));
        this.offsets = inRange.toArray(int[][]::new);
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean onLand = isLand(context, cursor, pos.getX(), pos.getZ());
        if (onLand && !includeLand) {
            return false;
        }

        float distance = edgeDistance(context, cursor, pos, !onLand);
        if (distance > maxDistance) {
            return false;
        }
        return random.nextFloat() < (maxDistance + 1.0F - distance) / maxDistance;
    }

    private float edgeDistance(PlacementContext context, BlockPos.MutableBlockPos cursor, BlockPos pos,
            boolean seekingLand) {
        for (int[] offset : offsets) {
            if (isLand(context, cursor, pos.getX() + offset[0], pos.getZ() + offset[1]) == seekingLand) {
                return Mth.sqrt(offset[0] * offset[0] + offset[1] * offset[1]);
            }
        }
        return maxDistance + 1.0F;
    }

    private static boolean isLand(PlacementContext context, BlockPos.MutableBlockPos cursor, int x, int z) {
        int floor = context.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
        return !context.getBlockState(cursor.set(x, floor, z)).getFluidState().is(FluidTags.WATER);
    }

    @Override
    public PlacementModifierType<?> type() {
        return WildscapesPlacementModifiers.SHORE_DENSITY.get();
    }
}
