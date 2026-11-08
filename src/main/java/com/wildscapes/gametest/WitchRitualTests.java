package com.wildscapes.gametest;

import com.wildscapes.entity.AbominationEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class WitchRitualTests {
    private WitchRitualTests() {}

    public static void ritualTurnsTheToadIntoAnAbomination(GameTestHelper helper) {
        var level = helper.getLevel();
        Vec3 at = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(8, 3, 8)));
        var src = level.getServer().createCommandSourceStack().withLevel(level).withPosition(at);
        level.getServer().getCommands().performPrefixedCommand(src, "wildscapes ritual");

        AABB box = new AABB(at, at).inflate(10.0D);
        helper.assertTrue(level.getEntitiesOfClass(Frog.class, box).size() == 1, "no toad was put down");
        helper.assertTrue(level.getEntitiesOfClass(Witch.class, box).size() == 4, "the witches did not turn up");

        helper.succeedWhen(() -> {
            helper.assertTrue(level.getEntitiesOfClass(Frog.class, box).isEmpty(), "the toad is still there");
            helper.assertTrue(level.getEntitiesOfClass(AbominationEntity.class, box).size() == 1, "no abomination yet");
            level.getEntitiesOfClass(AbominationEntity.class, box).forEach(AbominationEntity::discard);
            level.getEntitiesOfClass(Witch.class, box).forEach(Witch::discard);
        });
    }
}
