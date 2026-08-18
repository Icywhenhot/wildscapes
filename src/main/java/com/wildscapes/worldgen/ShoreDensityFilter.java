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

/**
 * Keeps only positions within {@code max_distance} blocks of the water's edge, and thins them
 * out the further from it they are. Hugging the bank is a certainty; density falls away linearly
 * and reaches nothing a block past the limit, so plants fringe the water's edge instead of
 * carpeting the whole pond.
 *
 * <p>By default only positions standing in the water count, which is what something floating
 * like duckweed wants. Setting {@code include_land} lets the dry side of the bank through as
 * well, measured the same way but towards the water — a plant that is happy in the shallows and
 * on the mud beside them then grows on both sides of the waterline.
 */
public class ShoreDensityFilter extends PlacementFilter {
    public static final MapCodec<ShoreDensityFilter> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.intRange(1, 16).fieldOf("max_distance").forGetter(filter -> filter.maxDistance),
                    Codec.BOOL.optionalFieldOf("include_land", false).forGetter(filter -> filter.includeLand))
                    .apply(instance, ShoreDensityFilter::new));

    private final int maxDistance;
    private final boolean includeLand;
    /** Offsets within range, nearest first, so a scan can stop at the first hit it finds. */
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
        // Reused across the scan below; a field would race, since one filter instance is
        // shared by every chunk being generated.
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean onLand = isLand(context, cursor, pos.getX(), pos.getZ());
        if (onLand && !includeLand) {
            return false;
        }
        // Whichever side of the waterline we are on, the edge is the nearest column of the other.
        float distance = edgeDistance(context, cursor, pos, !onLand);
        if (distance > maxDistance) {
            return false;
        }
        return random.nextFloat() < (maxDistance + 1.0F - distance) / maxDistance;
    }

    /**
     * Distance to the closest column that is land (or water, when {@code seekingLand} is false),
     * or more than {@code maxDistance} if there is none. Because the offsets are sorted, the
     * first hit is the nearest and the rest can be skipped — which matters, as this runs once
     * per placement attempt.
     */
    private float edgeDistance(PlacementContext context, BlockPos.MutableBlockPos cursor, BlockPos pos,
            boolean seekingLand) {
        for (int[] offset : offsets) {
            if (isLand(context, cursor, pos.getX() + offset[0], pos.getZ() + offset[1]) == seekingLand) {
                return Mth.sqrt(offset[0] * offset[0] + offset[1] * offset[1]);
            }
        }
        return maxDistance + 1.0F;
    }

    /**
     * A column counts as land when the space just above its floor is not water. Comparing
     * the two heightmaps instead would misread a grassy bank as water, because grass is not
     * motion-blocking and so does not register on the ocean floor map.
     */
    private static boolean isLand(PlacementContext context, BlockPos.MutableBlockPos cursor, int x, int z) {
        int floor = context.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
        return !context.getBlockState(cursor.set(x, floor, z)).getFluidState().is(FluidTags.WATER);
    }

    @Override
    public PlacementModifierType<?> type() {
        return WildscapesPlacementModifiers.SHORE_DENSITY.get();
    }
}
