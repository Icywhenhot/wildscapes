package com.wildscapes.gametest;

import java.util.List;

import com.wildscapes.worldgen.ClearProcessor;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wildscapes")
@PrefixGameTestTemplate(false)
public class VillageTerrainTests {
    private static final String PLATFORM = "gametest/tree_platform";

    private static void sweep(GameTestHelper helper, BlockPos where) {
        List<StructureTemplate.StructureBlockInfo> piece = List.of(
                new StructureTemplate.StructureBlockInfo(where,
                        Blocks.OAK_PLANKS.defaultBlockState(), null));
        new ClearProcessor(1, 3).finalizeProcessing(helper.getLevel(), where, where,
                piece, piece, new StructurePlaceSettings());
    }

    private static void announce(GameTestHelper helper, BlockPos where, BlockState state) {
        List<StructureTemplate.StructureBlockInfo> piece = List.of(
                new StructureTemplate.StructureBlockInfo(where, state, null));
        new ClearProcessor(1, 3).finalizeProcessing(helper.getLevel(), where, where,
                piece, piece, new StructurePlaceSettings());
    }

    @GameTest(template = PLATFORM)
    public static void aTurfFloorSurvivesTheNeighbour(GameTestHelper helper) {
        BlockPos turf = helper.absolutePos(new BlockPos(4, 3, 4));
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();

        helper.getLevel().setBlock(turf.below(), Blocks.AIR.defaultBlockState(), 18);
        announce(helper, turf, grass);
        helper.getLevel().setBlock(turf, grass, 18);

        sweep(helper, helper.absolutePos(new BlockPos(4, 2, 5)));

        helper.assertBlockPresent(Blocks.GRASS_BLOCK, new BlockPos(4, 3, 4));
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void groundOnDeckingSurvivesUnannounced(GameTestHelper helper) {
        helper.getLevel().setBlock(helper.absolutePos(new BlockPos(9, 2, 9)),
                Blocks.OAK_PLANKS.defaultBlockState(), 18);
        helper.getLevel().setBlock(helper.absolutePos(new BlockPos(9, 3, 9)),
                Blocks.GRASS_BLOCK.defaultBlockState(), 18);

        sweep(helper, helper.absolutePos(new BlockPos(9, 2, 10)));

        helper.assertBlockPresent(Blocks.GRASS_BLOCK, new BlockPos(9, 3, 9));
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void aHillsideIsStillDugOut(GameTestHelper helper) {
        helper.getLevel().setBlock(helper.absolutePos(new BlockPos(15, 2, 15)),
                Blocks.DIRT.defaultBlockState(), 18);
        helper.getLevel().setBlock(helper.absolutePos(new BlockPos(15, 3, 15)),
                Blocks.GRASS_BLOCK.defaultBlockState(), 18);

        sweep(helper, helper.absolutePos(new BlockPos(15, 2, 16)));

        helper.assertBlockPresent(Blocks.AIR, new BlockPos(15, 3, 15));
        helper.succeed();
    }
}
