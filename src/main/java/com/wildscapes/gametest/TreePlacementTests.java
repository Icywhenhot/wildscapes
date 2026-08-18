package com.wildscapes.gametest;

import java.util.Optional;

import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.worldgen.TemplateTreeConfiguration;
import com.wildscapes.worldgen.WildscapesFeatures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Checks that the hand-built trees actually come out of their structure files and come out as
 * cypress. The feature is called directly rather than through worldgen, since what is worth
 * pinning down here is the loading and the block swap, not which chunks it lands in.
 */
@GameTestHolder("wildscapes")
@PrefixGameTestTemplate(false)
public class TreePlacementTests {

    /** Resolved against {@link GameTestHolder}'s namespace; the class-name prefix is off above. */
    private static final String PLATFORM = "gametest/tree_platform";

    /**
     * The first free block above the platform. Game tests place their structure one above the
     * marker block, so the dirt floor of {@code tree_platform} lands at y 1, not y 0.
     */
    private static final BlockPos GROUND = new BlockPos(12, 2, 12);

    @GameTest(template = PLATFORM)
    public static void landTreeGrowsAsCypress(GameTestHelper helper) {
        boolean placed = grow(helper, GROUND, "wildscapes:tree/land/tree1",
                TemplateTreeConfiguration.Ground.LAND, -1);
        helper.assertTrue(placed, "the land tree refused to place on dirt; at the spot: "
                + helper.getBlockState(GROUND) + ", below it: " + helper.getBlockState(GROUND.below()));

        helper.succeedWhen(() -> {
            assertPresent(helper, WildscapesBlocks.CYPRESS_LOG.get().defaultBlockState(), "cypress log");
            assertAbsent(helper, Blocks.DARK_OAK_LOG.defaultBlockState(), "dark oak log");
            assertAbsent(helper, Blocks.MANGROVE_LEAVES.defaultBlockState(), "mangrove leaves");
        });
    }

    /**
     * tree2 carries a vine saved clinging to nothing, which has no model and leaves a hole in the
     * drape it belongs to. Every vine the tree puts down should end up hanging off something.
     */
    @GameTest(template = PLATFORM)
    public static void noVineIsLeftClingingToNothing(GameTestHelper helper) {
        helper.assertTrue(grow(helper, GROUND, "wildscapes:tree/land/tree2",
                TemplateTreeConfiguration.Ground.LAND, -1), "the tree refused to place");

        helper.succeedWhen(() -> forEachVine(helper, (pos, vine) -> {
            if (!anyFace(vine)) {
                throw new AssertionError("a vine at " + pos + " is clinging to nothing");
            }
            // up-only is the state that renders as a flat sheet instead of hanging.
            if (vine.getValue(VineBlock.UP) && !anySideFace(vine)) {
                throw new AssertionError("a vine at " + pos + " is pinned flat to the ceiling");
            }
        }));
    }

    /** Walks every vine the tree put down, a good margin past the platform. */
    private static void forEachVine(GameTestHelper helper, java.util.function.BiConsumer<BlockPos, BlockState> check) {
        for (int x = 0; x < 24; x++) {
            for (int y = 0; y < 20; y++) {
                for (int z = 0; z < 24; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = helper.getBlockState(pos);
                    if (state.is(Blocks.VINE)) {
                        check.accept(pos, state);
                    }
                }
            }
        }
    }

    private static boolean anyFace(BlockState vine) {
        return vine.getValue(VineBlock.UP) || anySideFace(vine);
    }

    private static boolean anySideFace(BlockState vine) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (vine.getValue(VineBlock.getPropertyForFace(side))) {
                return true;
            }
        }
        return false;
    }

    /**
     * The same rule for the four model-built trees a sapling grows. They are no longer part of
     * worldgen, but a sapling still puts one down, and they used to pin a vine to the ceiling
     * whenever it had a branch above it and nothing beside it.
     */
    @GameTest(template = PLATFORM)
    public static void saplingTreeHangsNoFlatVines(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        boolean placed = WildscapesFeatures.CYPRESS_TREE.get().place(new FeaturePlaceContext<>(
                Optional.empty(), level, level.getChunkSource().getGenerator(), level.getRandom(),
                helper.absolutePos(GROUND), NoneFeatureConfiguration.INSTANCE));
        helper.assertTrue(placed, "the sapling tree refused to grow on dirt");
        helper.succeedWhen(() -> forEachVine(helper, (pos, vine) -> {
            if (vine.getValue(VineBlock.UP) && !anySideFace(vine)) {
                throw new AssertionError("a vine at " + pos + " is pinned flat to the ceiling");
            }
        }));
    }

    @GameTest(template = PLATFORM)
    public static void treesRefuseToGrowThroughEachOther(GameTestHelper helper) {
        helper.assertTrue(grow(helper, GROUND, "wildscapes:tree/land/tree1",
                TemplateTreeConfiguration.Ground.LAND, -1), "the first tree refused to place");
        // Tried repeatedly: rotation and mirror are rolled per attempt, and a tree turned a
        // different way used to be able to thread itself through the gaps in the first one.
        for (int attempt = 0; attempt < 12; attempt++) {
            helper.assertFalse(grow(helper, GROUND, "wildscapes:tree/land/tree1",
                    TemplateTreeConfiguration.Ground.LAND, -1),
                    "a second tree grew through the first on attempt " + attempt);
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void landTreeRefusesWater(GameTestHelper helper) {
        helper.setBlock(GROUND, Blocks.WATER);
        boolean placed = grow(helper, GROUND, "wildscapes:tree/land/tree1",
                TemplateTreeConfiguration.Ground.LAND, -1);
        helper.assertFalse(placed, "a land tree planted itself in water");
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void waterTreeRefusesDryLand(GameTestHelper helper) {
        boolean placed = grow(helper, GROUND, "wildscapes:tree/water/watertree1",
                TemplateTreeConfiguration.Ground.WATER, 0);
        helper.assertFalse(placed, "a water tree planted itself on dry land");
        helper.succeed();
    }

    /** Runs the feature at a spot inside the test area, as worldgen would. */
    private static boolean grow(GameTestHelper helper, BlockPos relative, String template,
            TemplateTreeConfiguration.Ground ground, int yOffset) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(relative);
        return WildscapesFeatures.TEMPLATE_TREE.get().place(new FeaturePlaceContext<>(
                Optional.empty(),
                level,
                level.getChunkSource().getGenerator(),
                level.getRandom(),
                origin,
                new TemplateTreeConfiguration(
                        java.util.List.of(ResourceLocation.parse(template)), ground, yOffset)));
    }

    private static void assertPresent(GameTestHelper helper, BlockState state, String what) {
        if (!anyBlock(helper, state)) {
            throw new AssertionError("no " + what + " anywhere in the placed tree");
        }
    }

    private static void assertAbsent(GameTestHelper helper, BlockState state, String what) {
        if (anyBlock(helper, state)) {
            throw new AssertionError(what + " survived the swap to cypress");
        }
    }

    private static boolean anyBlock(GameTestHelper helper, BlockState state) {
        for (int x = 0; x < 24; x++) {
            for (int y = 0; y < 20; y++) {
                for (int z = 0; z < 24; z++) {
                    if (helper.getBlockState(new BlockPos(x, y, z)).is(state.getBlock())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
