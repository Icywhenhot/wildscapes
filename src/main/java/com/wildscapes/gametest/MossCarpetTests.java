package com.wildscapes.gametest;

import com.wildscapes.block.DrapingMossCarpetBlock;
import com.wildscapes.block.DrapingMossCarpetBlock.Drape;
import com.wildscapes.block.WildscapesBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Bone meal on a moss carpet should visibly hurry the drape along rather than doing nothing. */
@GameTestHolder("wildscapes")
@PrefixGameTestTemplate(false)
public class MossCarpetTests {

    private static final String PLATFORM = "gametest/tree_platform";

    /** A lone block standing proud of the floor, so the carpet on it has open air on all sides. */
    private static final BlockPos PILLAR = new BlockPos(12, 2, 12);
    private static final BlockPos CARPET = new BlockPos(12, 3, 12);

    @GameTest(template = PLATFORM)
    public static void bonemealGrowsADrape(GameTestHelper helper) {
        helper.setBlock(PILLAR, Blocks.STONE);
        helper.setBlock(CARPET, WildscapesBlocks.MOSS_CARPET.get().defaultBlockState());

        BlockState bare = helper.getBlockState(CARPET);
        helper.assertTrue(sidesGrown(bare) == 0, "the carpet did not start bare");

        DrapingMossCarpetBlock carpet = WildscapesBlocks.MOSS_CARPET.get();
        helper.assertTrue(
                carpet.isValidBonemealTarget(helper.getLevel(), helper.absolutePos(CARPET), bare),
                "a bare carpet with open air all round it refused bone meal");
        carpet.performBonemeal(helper.getLevel(), helper.getLevel().getRandom(),
                helper.absolutePos(CARPET), bare);

        helper.succeedWhen(() -> {
            BlockState grown = helper.getBlockState(CARPET);
            if (sidesGrown(grown) == 0) {
                throw new AssertionError("bone meal left the carpet as bare as it found it");
            }
        });
    }

    /** Bone meal on a plain vanilla carpet should hand it over to the draping one first. */
    @GameTest(template = PLATFORM)
    public static void vanillaCarpetIsHandedOver(GameTestHelper helper) {
        helper.setBlock(PILLAR, Blocks.STONE);
        helper.setBlock(CARPET, Blocks.MOSS_CARPET);

        net.minecraft.world.entity.player.Player player =
                helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BONE_MEAL));
        com.wildscapes.block.MossCarpetSwap.onBonemeal(
                new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(
                        player,
                        net.minecraft.world.InteractionHand.MAIN_HAND,
                        helper.absolutePos(CARPET),
                        new net.minecraft.world.phys.BlockHitResult(
                                net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(CARPET)),
                                Direction.UP, helper.absolutePos(CARPET), false)));

        helper.succeedWhen(() -> helper.assertBlockPresent(WildscapesBlocks.MOSS_CARPET.get(), CARPET));
    }

    private static int sidesGrown(BlockState carpet) {
        int grown = 0;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (carpet.getValue(DrapingMossCarpetBlock.sideFacing(side)) != Drape.NONE) {
                grown++;
            }
        }
        return grown;
    }
}
