package com.wildscapes.gametest;

import java.util.List;

import com.wildscapes.worldgen.ConnectProcessor;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wildscapes")
@PrefixGameTestTemplate(false)
public class VillageJoinTests {
    private static final String PLATFORM = "gametest/tree_platform";

    @GameTest(template = PLATFORM)
    public static void railingsMeetAcrossASeam(GameTestHelper helper) {
        BlockPos standing = helper.absolutePos(new BlockPos(6, 3, 6));
        BlockPos arriving = standing.east();

        BlockState bare = Blocks.DARK_OAK_FENCE.defaultBlockState();
        helper.getLevel().setBlock(standing, bare, 18);

        List<StructureTemplate.StructureBlockInfo> incoming = List.of(
                new StructureTemplate.StructureBlockInfo(arriving, bare, null));

        List<StructureTemplate.StructureBlockInfo> mended = ConnectProcessor.INSTANCE
                .finalizeProcessing(helper.getLevel(), arriving, arriving,
                        incoming, incoming, new StructurePlaceSettings());

        BlockState came = mended.get(0).state();
        helper.assertTrue(came.getValue(BlockStateProperties.WEST),
                "the arriving railing did not reach back towards the one already standing");

        BlockState stood = helper.getLevel().getBlockState(standing);
        helper.assertTrue(stood.getValue(BlockStateProperties.EAST),
                "the standing railing was never told there was now something to reach for");
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void railingsMeetOnATurnedPiece(GameTestHelper helper) {
        BlockPos standing = helper.absolutePos(new BlockPos(14, 3, 6));
        BlockPos arriving = standing.west();

        BlockState bare = Blocks.DARK_OAK_FENCE.defaultBlockState();
        helper.getLevel().setBlock(standing, bare, 18);

        List<StructureTemplate.StructureBlockInfo> incoming = List.of(
                new StructureTemplate.StructureBlockInfo(arriving, bare, null));

        StructurePlaceSettings turned = new StructurePlaceSettings()
                .setRotation(Rotation.CLOCKWISE_90);
        List<StructureTemplate.StructureBlockInfo> mended = ConnectProcessor.INSTANCE
                .finalizeProcessing(helper.getLevel(), arriving, arriving,
                        incoming, incoming, turned);

        BlockState came = mended.get(0).state().rotate(Rotation.CLOCKWISE_90);
        helper.assertTrue(came.getValue(BlockStateProperties.EAST),
                "a turned piece's railing reached the wrong way: the quarter turn was applied twice");

        BlockState stood = helper.getLevel().getBlockState(standing);
        helper.assertTrue(stood.getValue(BlockStateProperties.WEST),
                "the standing railing was never told there was now something to reach for");
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void aRunOfRailingSurvivesBeingTurned(GameTestHelper helper) {
        BlockPos west = helper.absolutePos(new BlockPos(4, 5, 14));
        BlockPos east = west.east();

        BlockState bare = Blocks.DARK_OAK_FENCE.defaultBlockState();
        List<StructureTemplate.StructureBlockInfo> incoming = List.of(
                new StructureTemplate.StructureBlockInfo(west, bare, null),
                new StructureTemplate.StructureBlockInfo(east, bare, null));

        StructurePlaceSettings turned = new StructurePlaceSettings()
                .setRotation(Rotation.CLOCKWISE_90);
        List<StructureTemplate.StructureBlockInfo> mended = ConnectProcessor.INSTANCE
                .finalizeProcessing(helper.getLevel(), west, west, incoming, incoming, turned);

        BlockState left = mended.get(0).state().rotate(Rotation.CLOCKWISE_90);
        BlockState right = mended.get(1).state().rotate(Rotation.CLOCKWISE_90);
        helper.assertTrue(left.getValue(BlockStateProperties.EAST),
                "a rail inside a turned piece lost the neighbour it was filed beside");
        helper.assertTrue(right.getValue(BlockStateProperties.WEST),
                "a rail inside a turned piece lost the neighbour it was filed beside");
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void aLoneRailingStaysBare(GameTestHelper helper) {
        BlockPos alone = helper.absolutePos(new BlockPos(10, 5, 10));
        BlockState bare = Blocks.DARK_OAK_FENCE.defaultBlockState();

        List<StructureTemplate.StructureBlockInfo> incoming = List.of(
                new StructureTemplate.StructureBlockInfo(alone, bare, null));
        List<StructureTemplate.StructureBlockInfo> mended = ConnectProcessor.INSTANCE
                .finalizeProcessing(helper.getLevel(), alone, alone,
                        incoming, incoming, new StructurePlaceSettings());

        BlockState came = mended.get(0).state();
        for (var side : new net.minecraft.world.level.block.state.properties.BooleanProperty[] {
                BlockStateProperties.NORTH, BlockStateProperties.SOUTH,
                BlockStateProperties.EAST, BlockStateProperties.WEST }) {
            helper.assertFalse(came.getValue(side),
                    "a railing with nothing beside it reached out anyway");
        }
        helper.succeed();
    }
}
