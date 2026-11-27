package com.wildscapes.gametest;

import net.minecraft.world.entity.EntityType;
import com.wildscapes.effect.WildscapesEffects;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;

public final class IllusionerMirageTests {
    private static final String PLATFORM = "gametest/tree_platform";

    private IllusionerMirageTests() {}

    public static void zombieGetsMirage(GameTestHelper helper) {
        var player = helper.makeMockSurvivalPlayer();
        player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 3, 9))));
        var illusioner = helper.spawn(EntityType.ILLUSIONER, new BlockPos(5, 3, 5));
        var zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(7, 3, 5));
        zombie.setNoAi(true);
        illusioner.setTarget(player);
        zombie.setTarget(player);
        helper.onEachTick(() -> illusioner.setTarget(player));
        helper.runAtTickTime(8, () -> {
            helper.assertTrue(illusioner.isCastingSpell(), "illusioner did not start casting");
            helper.assertTrue(!illusioner.hasEffect(MobEffects.INVISIBILITY), "self cloning took priority");
            helper.assertTrue(!zombie.hasEffect(WildscapesEffects.MIRAGE.get()), "mirage skipped its warmup");
        });

        helper.succeedWhen(() -> helper.assertTrue(zombie.hasEffect(WildscapesEffects.MIRAGE.get()),
                "zombie never received mirage"));
    }

    public static void cloneCooldownAndMemory(GameTestHelper helper) {
        var player = helper.makeMockSurvivalPlayer();
        player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 3, 9))));
        var illusioner = helper.spawn(EntityType.ILLUSIONER, new BlockPos(5, 3, 5));
        var first = helper.spawn(EntityType.ZOMBIE, new BlockPos(7, 3, 5));
        var second = helper.spawn(EntityType.ZOMBIE, new BlockPos(8, 3, 5));
        first.setNoAi(true);
        second.setNoAi(true);
        first.setInvulnerable(true);
        second.setInvulnerable(true);
        first.setTarget(player);
        second.setTarget(player);
        int[] casts = {-1, -1};
        helper.onEachTick(() -> {
            illusioner.setTarget(player);
            if (first.hasEffect(WildscapesEffects.MIRAGE.get())) {
                helper.assertTrue(casts[0] == -1, "same zombie was cloned twice");
                casts[0] = illusioner.tickCount;
                first.removeEffect(WildscapesEffects.MIRAGE.get());
            }
            if (second.hasEffect(WildscapesEffects.MIRAGE.get())) {
                helper.assertTrue(casts[0] >= 0, "nearest attacker was skipped");
                helper.assertTrue(casts[1] == -1, "second zombie was cloned twice");
                helper.assertTrue(illusioner.tickCount - casts[0] >= 200, "cloning ignored the ten second cooldown");
                casts[1] = illusioner.tickCount;
                second.removeEffect(WildscapesEffects.MIRAGE.get());
            }
        });
        helper.runAtTickTime(510, () -> {
            helper.assertTrue(casts[0] >= 0 && casts[1] >= 0, "both attackers were not cloned");
            helper.succeed();
        });
    }
}
