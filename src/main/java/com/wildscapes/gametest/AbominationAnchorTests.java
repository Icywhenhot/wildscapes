package com.wildscapes.gametest;

import com.wildscapes.entity.AbominationEntity;
import com.wildscapes.entity.WildscapesEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wildscapes")
@PrefixGameTestTemplate(false)
public final class AbominationAnchorTests {
    private static final String PLATFORM = "gametest/tree_platform";

    private AbominationAnchorTests() {}

    @GameTest(template = PLATFORM, timeoutTicks = 600)
    public static void wandersBackToItsAnchor(GameTestHelper helper) {
        BlockPos anchor = helper.absolutePos(new BlockPos(4, 3, 4));
        AbominationEntity mob = spawnAt(helper.getLevel(), anchor);
        helper.assertTrue(anchor.equals(mob.getRestrictCenter()), "it did not take its spawn point as an anchor");

        Vec3 far = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(20, 3, 20)));
        mob.teleportTo(far.x, far.y, far.z);
        Vec3 home = Vec3.atBottomCenterOf(anchor);
        helper.succeedWhen(() -> helper.assertTrue(
                mob.position().distanceTo(home) <= AbominationEntity.ANCHOR_RADIUS,
                "still " + (int) mob.position().distanceTo(home) + " blocks from its anchor"));
    }

    @GameTest(template = PLATFORM)
    public static void anchorSurvivesASave(GameTestHelper helper) {
        BlockPos anchor = helper.absolutePos(new BlockPos(6, 3, 6));
        AbominationEntity mob = spawnAt(helper.getLevel(), anchor);
        CompoundTag saved = mob.saveWithoutId(new CompoundTag());
        mob.discard();

        AbominationEntity loaded = WildscapesEntities.ABOMINATION.get().create(helper.getLevel());
        loaded.load(saved);
        helper.assertTrue(anchor.equals(loaded.getRestrictCenter())
                && loaded.getRestrictRadius() == AbominationEntity.ANCHOR_RADIUS,
                "the anchor was lost on reload: " + loaded.getRestrictCenter());
        helper.succeed();
    }

    private static AbominationEntity spawnAt(ServerLevel level, BlockPos pos) {
        AbominationEntity mob = WildscapesEntities.ABOMINATION.get().create(level);
        mob.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
        level.addFreshEntity(mob);
        return mob;
    }
}
