package com.wildscapes.gametest;

import java.util.List;

import com.wildscapes.entity.TonguePath;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wildscapes")
@PrefixGameTestTemplate(false)
public final class AbominationTongueTests {
    private static final String PLATFORM = "gametest/tree_platform";

    private AbominationTongueTests() {}

    @GameTest(template = PLATFORM)
    public static void tongueBendsAroundAWall(GameTestHelper helper) {
        for (int y = 3; y <= 8; y++) {
            for (int x = 4; x <= 14; x++) {
                helper.setBlock(new BlockPos(x, y, 9), Blocks.STONE);
            }
        }

        Vec3 mouth = mid(helper, new BlockPos(9, 4, 5));
        Vec3 prey = mid(helper, new BlockPos(9, 4, 13));
        helper.assertFalse(TonguePath.clear(helper.getLevel(), mouth, prey), "the wall did not block line of sight");

        List<Vec3> path = TonguePath.find(helper.getLevel(), mouth, prey);
        helper.assertTrue(path != null, "no tongue path around the wall");
        helper.assertTrue(path.size() > 2, "the tongue path stayed straight through the wall");
        for (int i = 1; i < path.size(); i++) {
            helper.assertTrue(TonguePath.clear(helper.getLevel(), path.get(i - 1), path.get(i)),
                    "tongue leg " + i + " passes through a block");
        }
        helper.assertTrue(path.get(0).equals(mouth) && path.get(path.size() - 1).equals(prey),
                "the tongue path does not run mouth to prey");
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void tongueGivesUpOnASealedTarget(GameTestHelper helper) {
        BlockPos prey = new BlockPos(9, 5, 13);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    helper.setBlock(prey.offset(x, y, z), Blocks.STONE);
                }
            }
        }

        helper.assertTrue(TonguePath.find(helper.getLevel(), mid(helper, new BlockPos(9, 5, 5)),
                mid(helper, prey)) == null, "the tongue found a way into sealed stone");
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void reelWalksTheWholePath(GameTestHelper helper) {
        List<Vec3> path = List.of(new Vec3(0, 0, 0), new Vec3(0, 0, 4), new Vec3(6, 0, 4));
        helper.assertTrue(TonguePath.pointAt(path, 0.0D).equals(path.get(0)), "reel did not start at the mouth");
        helper.assertTrue(TonguePath.pointAt(path, 1.0D).equals(path.get(2)), "reel did not end at the prey");
        helper.assertTrue(TonguePath.pointAt(path, 0.4D).equals(new Vec3(0, 0, 4)), "reel missed the corner");

        Vec3 prev = TonguePath.pointAt(path, 0.0D);
        double walked = 0.0D;
        for (int i = 1; i <= 20; i++) {
            Vec3 next = TonguePath.pointAt(path, i / 20.0D);
            walked += next.distanceTo(prev);
            prev = next;
        }
        helper.assertTrue(Math.abs(walked - 10.0D) < 1.0E-6D, "reel covered " + walked + " instead of 10");
        helper.succeed();
    }

    private static Vec3 mid(GameTestHelper helper, BlockPos pos) {
        return Vec3.atCenterOf(helper.absolutePos(pos));
    }
}
