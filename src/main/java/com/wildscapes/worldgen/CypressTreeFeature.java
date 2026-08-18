package com.wildscapes.worldgen;

import com.mojang.serialization.Codec;
import com.wildscapes.block.WildscapesBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Places one of the four cypress trees extracted verbatim from the reference
 * model ({@link CypressTreeTemplates}). Over water a stilt-rooted "water" tree
 * is chosen (roots continue below the waterline); on dirt/mud a solid-trunk
 * "land" tree. The chosen template is randomly rotated/mirrored for variety.
 * <p>
 * Foliage is placed as <b>persistent</b> cypress leaves so the flat,
 * largely-detached model canopies never decay. The model's hanging vines are
 * placed as real vine blocks whose attachment faces are computed against the
 * placed tree, and the big water tree keeps its moss-draped root.
 */
public class CypressTreeFeature extends Feature<NoneFeatureConfiguration> {
    public CypressTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockState below = level.getBlockState(origin.below());
        boolean overWater = below.getFluidState().is(FluidTags.WATER)
                || level.getBlockState(origin).getFluidState().is(FluidTags.WATER);

        int[][][] logsSet;
        int[][][] leavesSet;
        int[][][] vinesSet;
        int[][][] mossSet;
        int count;
        if (overWater) {
            logsSet = CypressTreeTemplates.WATER_LOGS;
            leavesSet = CypressTreeTemplates.WATER_LEAVES;
            vinesSet = CypressTreeTemplates.WATER_VINES;
            mossSet = CypressTreeTemplates.WATER_MOSS;
            count = CypressTreeTemplates.WATER_COUNT;
        } else if (below.is(BlockTags.DIRT) || below.is(Blocks.MUD) || below.is(Blocks.CLAY)) {
            logsSet = CypressTreeTemplates.LAND_LOGS;
            leavesSet = CypressTreeTemplates.LAND_LEAVES;
            vinesSet = CypressTreeTemplates.LAND_VINES;
            mossSet = CypressTreeTemplates.LAND_MOSS;
            count = CypressTreeTemplates.LAND_COUNT;
        } else {
            return false;
        }

        int idx = random.nextInt(count);
        int rotation = random.nextInt(4);
        boolean mirror = random.nextBoolean();
        int[][] tLogs = logsSet[idx];
        int[][] tLeaves = leavesSet[idx];
        int[][] tVines = vinesSet[idx];
        int[][] tMoss = mossSet[idx];

        // Don't grow through a tree that is already standing here. Checked over the wood and the
        // canopy both, so two trees may stand close but never occupy the same blocks.
        for (int[][] part : new int[][][] { tLogs, tLeaves }) {
            for (int[] c : part) {
                if (occupied(level.getBlockState(transform(origin, c, rotation, mirror)))) {
                    return false;
                }
            }
        }

        // Don't consume a sapling if the trunk/canopy space above ground is obstructed.
        for (int[] c : tLogs) {
            if (c[1] >= 1 && !canReplace(level, transform(origin, c, rotation, mirror))) {
                return false;
            }
        }

        BlockState log = WildscapesBlocks.CYPRESS_LOG.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        for (int[] c : tLogs) {
            placeLog(level, transform(origin, c, rotation, mirror), log);
        }

        BlockState leaf = WildscapesBlocks.CYPRESS_LEAVES.get().defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true)
                .setValue(LeavesBlock.DISTANCE, 1);
        for (int[] c : tLeaves) {
            placeLeaf(level, transform(origin, c, rotation, mirror), leaf);
        }

        // Vines attach to whatever tree blocks ended up adjacent, so they need no
        // rotation/mirror bookkeeping; sort top-down so hanging chains link up.
        int[][] vines = tVines.clone();
        java.util.Arrays.sort(vines, (a, b) -> Integer.compare(b[1], a[1]));
        for (int[] c : vines) {
            placeVine(level, transform(origin, c, rotation, mirror));
        }

        for (int[] c : tMoss) {
            placeMoss(level, transform(origin, c, rotation, mirror));
        }
        return true;
    }

    /** Mirror on X (optional) then rotate 90 degrees {@code rotation} times about Y. */
    private static BlockPos transform(BlockPos origin, int[] c, int rotation, boolean mirror) {
        int x = mirror ? -c[0] : c[0];
        int z = c[2];
        for (int i = 0; i < rotation; i++) {
            int nx = z;
            int nz = -x;
            x = nx;
            z = nz;
        }
        return origin.offset(x, c[1], z);
    }

    private void placeLog(WorldGenLevel level, BlockPos pos, BlockState log) {
        if (canReplace(level, pos)) {
            setBlock(level, pos, log);
        }
    }

    private void placeLeaf(WorldGenLevel level, BlockPos pos, BlockState leaf) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getFluidState().is(FluidTags.WATER)) {
            setBlock(level, pos, leaf);
        }
    }

    /**
     * Places a vine attached to adjacent tree blocks: side faces toward any neighbouring
     * log/leaf, otherwise carrying on the faces of the vine above it so a chain hangs as one
     * curtain.
     *
     * <p>A vine with nothing beside it is left out rather than pinned to the ceiling. Clinging
     * upward is a real vine state, but it is the one that renders as a flat sheet lying under the
     * block above instead of hanging from it, which reads as a glitch in a canopy.
     */
    private void placeVine(WorldGenLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir()) {
            return;
        }
        BlockState vine = Blocks.VINE.defaultBlockState();
        boolean attached = false;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (isTreeBlock(level.getBlockState(pos.relative(dir)))) {
                vine = vine.setValue(VineBlock.getPropertyForFace(dir), true);
                attached = true;
            }
        }
        if (!attached) {
            BlockState above = level.getBlockState(pos.above());
            if (above.is(Blocks.VINE)) {
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    if (above.getValue(VineBlock.getPropertyForFace(dir))) {
                        vine = vine.setValue(VineBlock.getPropertyForFace(dir), true);
                        attached = true;
                    }
                }
            }
        }
        if (attached) {
            setBlock(level, pos, vine);
        }
    }

    private void placeMoss(WorldGenLevel level, BlockPos pos) {
        if (level.getBlockState(pos).isAir() && isTreeBlock(level.getBlockState(pos.below()))) {
            setBlock(level, pos, Blocks.MOSS_CARPET.defaultBlockState());
        }
    }

    /** Space another tree has already taken — its wood, or a canopy of ours. */
    private static boolean occupied(BlockState state) {
        return state.is(BlockTags.LOGS) || state.is(WildscapesBlocks.CYPRESS_LEAVES.get());
    }

    private static boolean isTreeBlock(BlockState state) {
        return state.is(WildscapesBlocks.CYPRESS_LOG.get())
                || state.is(WildscapesBlocks.CYPRESS_LEAVES.get());
    }

    private static boolean canReplace(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir()
                || state.canBeReplaced()
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.SAPLINGS)
                || state.getFluidState().is(FluidTags.WATER);
    }
}
