package com.wildscapes.gametest;

import com.wildscapes.entity.AbominationEntity;
import com.wildscapes.entity.WildscapesEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;

public final class AbominationAnchorTests {
    private static final String PLATFORM = "gametest/tree_platform";

    private AbominationAnchorTests() {}

    public static void wandersBackToItsAnchor(GameTestHelper helper) {
        BlockPos anchor = helper.absolutePos(new BlockPos(4, 3, 4));
        AbominationEntity mob = spawnAt(helper.getLevel(), anchor);
        helper.assertTrue(anchor.equals(mob.getHomePosition()), "it did not take its spawn point as an anchor");

        Vec3 far = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(20, 3, 20)));
        mob.teleportTo(far.x, far.y, far.z);
        Vec3 home = Vec3.atBottomCenterOf(anchor);
        helper.succeedWhen(() -> helper.assertTrue(
                mob.position().distanceTo(home) <= AbominationEntity.ANCHOR_RADIUS,
                "still " + (int) mob.position().distanceTo(home) + " blocks from its anchor"));
    }

    public static void anchorSurvivesASave(GameTestHelper helper) {
        BlockPos anchor = helper.absolutePos(new BlockPos(6, 3, 6));
        AbominationEntity mob = spawnAt(helper.getLevel(), anchor);
        var saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        mob.saveWithoutId(saved);
        mob.discard();

        AbominationEntity loaded = WildscapesEntities.ABOMINATION.create(helper.getLevel(), EntitySpawnReason.LOAD);
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved.buildResult()));
        helper.assertTrue(anchor.equals(loaded.getHomePosition())
                && loaded.getHomeRadius() == AbominationEntity.ANCHOR_RADIUS,
                "the anchor was lost on reload: " + loaded.getHomePosition());
        helper.succeed();
    }

    private static AbominationEntity spawnAt(ServerLevel level, BlockPos pos) {
        AbominationEntity mob = WildscapesEntities.ABOMINATION.create(level, EntitySpawnReason.STRUCTURE);
        mob.snapTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
        level.addFreshEntity(mob);
        return mob;
    }
}
