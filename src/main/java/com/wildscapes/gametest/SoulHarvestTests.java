package com.wildscapes.gametest;

import com.wildscapes.block.CauldronOfSoulsBlock;
import com.wildscapes.block.IncendiaryFireBlock;
import com.wildscapes.block.SummoningBonfireBlock;
import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.block.entity.CauldronOfSoulsBlockEntity;
import com.wildscapes.effect.WildscapesEffects;
import com.wildscapes.entity.AbominationEntity;
import com.wildscapes.entity.SoulHarvest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wildscapes")
@PrefixGameTestTemplate(false)
public final class SoulHarvestTests {
    private static final String PLATFORM = "gametest/tree_platform";

    private SoulHarvestTests() {}

    @SuppressWarnings("removal")
    @GameTest(template = PLATFORM, timeoutTicks = 4000, batch = "soul_harvest")
    public static void omenStartsItAndSoulsFinishIt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos cauldron = new BlockPos(12, 2, 12);
        BlockPos[] fires = {new BlockPos(4, 2, 4), new BlockPos(19, 2, 19), new BlockPos(4, 2, 19)};
        helper.setBlock(cauldron, WildscapesBlocks.CAULDRON_OF_SOULS.get().defaultBlockState());
        for (BlockPos f : fires) {
            helper.setBlock(f, WildscapesBlocks.SUMMONING_BONFIRE.get().defaultBlockState());
        }

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Vec3 stand = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(12, 2, 8)));
        player.teleportTo(level, stand.x, stand.y, stand.z, 0.0F, 0.0F);
        player.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 2000, 0));

        helper.runAfterDelay(2, () -> {
            CauldronOfSoulsBlockEntity.tryStart(level, player);
            helper.assertFalse(player.hasEffect(MobEffects.BAD_OMEN), "the cauldron did not take the omen");
            helper.assertTrue(player.hasEffect(WildscapesEffects.SOUL_HARVEST), "no soul_harvest icon while it runs");
            helper.assertBlockProperty(cauldron, CauldronOfSoulsBlock.PHASE, CauldronOfSoulsBlock.Phase.ACTIVE);
            for (BlockPos f : fires) {
                helper.assertBlockProperty(f, SummoningBonfireBlock.LIT, true);
            }
        });

        AABB area = new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(64);
        boolean[] paid = {false};
        int[] abominations = {0};
        helper.onEachTick(() -> {
            for (Mob mob : level.getEntitiesOfClass(Mob.class, area, m -> m.hasData(SoulHarvest.ORIGIN) && m.isAlive())) {
                if (mob instanceof AbominationEntity) {
                    abominations[0]++;
                }
                mob.hurt(level.damageSources().playerAttack(player), 10000.0F);
            }
            CauldronOfSoulsBlock.Phase phase = helper.getBlockState(cauldron).getValue(CauldronOfSoulsBlock.PHASE);
            if (!paid[0]) {
                if (phase != CauldronOfSoulsBlock.Phase.REWARDS
                        || level.getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(cauldron)).inflate(4)).isEmpty()) {
                    return;
                }
                helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, area, i -> i.distanceToSqr(Vec3.atCenterOf(helper.absolutePos(cauldron))) > 36).isEmpty(),
                        "an soul_harvest mob dropped loot");
                helper.assertTrue(abominations[0] == 1, "expected exactly one abomination, got " + abominations[0]);
                helper.assertFalse(player.hasEffect(WildscapesEffects.SOUL_HARVEST), "soul_harvest icon outlived the raid");
                paid[0] = true;
                return;
            }
            if (phase != CauldronOfSoulsBlock.Phase.INACTIVE) {
                return;
            }
            player.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 2000, 0));
            CauldronOfSoulsBlockEntity.tryStart(level, player);
            helper.assertBlockProperty(cauldron, CauldronOfSoulsBlock.PHASE, CauldronOfSoulsBlock.Phase.ACTIVE);
            helper.setBlock(cauldron, Blocks.AIR.defaultBlockState());
            level.getServer().getPlayerList().remove(player);
            helper.succeed();
        });
    }

    @SuppressWarnings("removal")
    @GameTest(template = PLATFORM, timeoutTicks = 1200, batch = "soul_harvest_locals")
    public static void campMobsFeedTheBar(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos cauldron = new BlockPos(12, 2, 12);
        helper.setBlock(cauldron, WildscapesBlocks.CAULDRON_OF_SOULS.get().defaultBlockState());
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Vec3 stand = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(12, 2, 8)));
        player.teleportTo(level, stand.x, stand.y, stand.z, 0.0F, 0.0F);
        player.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 2000, 0));
        helper.runAfterDelay(2, () -> CauldronOfSoulsBlockEntity.tryStart(level, player));

        helper.runAfterDelay(320, () -> {
            for (int i = 0; i < 10; i++) {
                Vindicator local = helper.spawn(EntityType.VINDICATOR, new BlockPos(6 + i, 2, 18));
                local.hurt(level.damageSources().playerAttack(player), 10000.0F);
            }
        });
        helper.onEachTick(() -> {
            if (helper.getTick() < 320
                    || helper.getBlockState(cauldron).getValue(CauldronOfSoulsBlock.PHASE) != CauldronOfSoulsBlock.Phase.REWARDS) {
                return;
            }
            helper.setBlock(cauldron, Blocks.AIR.defaultBlockState());
            level.getServer().getPlayerList().remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200)
    public static void gnashingRaisesFangs(GameTestHelper helper) {
        Pig pig = helper.spawn(EntityType.PIG, new BlockPos(8, 2, 8));
        pig.setNoAi(true);
        pig.addEffect(new MobEffectInstance(WildscapesEffects.GNASHING, 200, 2));
        helper.succeedWhen(() -> helper.assertTrue(
                !helper.getLevel().getEntitiesOfClass(EvokerFangs.class, pig.getBoundingBox().inflate(2)).isEmpty(),
                "no fangs rose under the pig"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100)
    public static void incendiaryFireSparesMobs(GameTestHelper helper) {
        BlockPos mid = new BlockPos(10, 2, 10);
        IncendiaryFireBlock.spread(helper.getLevel(), Vec3.atBottomCenterOf(helper.absolutePos(mid)));
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                helper.assertBlockPresent(WildscapesBlocks.INCENDIARY_FIRE.get(), mid.offset(dx, 0, dz));
            }
        }
        Pig pig = helper.spawn(EntityType.PIG, mid);
        pig.setNoAi(true);
        helper.runAfterDelay(40, () -> {
            helper.assertTrue(pig.getHealth() == pig.getMaxHealth() && !pig.isOnFire(), "the fire burned a pig");
            helper.succeed();
        });
    }
}
