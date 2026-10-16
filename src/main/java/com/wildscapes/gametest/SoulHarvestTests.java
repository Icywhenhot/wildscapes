package com.wildscapes.gametest;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.wildscapes.block.CauldronOfSoulsBlock;
import com.wildscapes.block.IncendiaryFireBlock;
import com.wildscapes.block.SummoningBonfireBlock;
import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.block.entity.CauldronOfSoulsBlockEntity;
import com.wildscapes.effect.WildscapesEffects;
import com.wildscapes.entity.AbominationEntity;
import com.wildscapes.entity.IncendiaryBottle;
import com.wildscapes.entity.SoulHarvest;

import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.network.CommonListenerCookie;
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
import io.netty.channel.embedded.EmbeddedChannel;

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

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "incendiary")
    public static void cauldronBottlesRiseAndHuntPlayers(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = new BlockPos(12, 2, 12);
        BlockPos at = helper.absolutePos(pos);
        helper.setBlock(pos, WildscapesBlocks.CAULDRON_OF_SOULS.get().defaultBlockState());
        ServerPlayer player = survivalPlayer(helper);
        Vec3 stand = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(12, 2, 6)));
        player.teleportTo(level, stand.x, stand.y, stand.z, 0, 0);
        player.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 2000));
        helper.runAfterDelay(2, () -> CauldronOfSoulsBlockEntity.tryStart(level, player));
        boolean[] rose = {false};
        AABB area = new AABB(at).inflate(20);
        helper.onEachTick(() -> {
            for (Mob mob : level.getEntitiesOfClass(Mob.class, area)) {
                mob.setNoAi(true);
            }
            for (IncendiaryBottle bottle : level.getEntitiesOfClass(IncendiaryBottle.class, area, b -> b.cameFrom(at))) {
                if (bottle.getY() > at.getY() + 2.0) {
                    rose[0] = true;
                }
            }
            if (!player.isOnFire()) {
                return;
            }
            helper.assertTrue(rose[0], "the bottle did not float above the cauldron before attacking");
            helper.assertTrue(player.getHealth() < player.getMaxHealth(), "the bottle did not hurt the player");
            helper.assertTrue(!level.getBlockStates(player.getBoundingBox().inflate(2))
                    .filter(s -> s.is(WildscapesBlocks.INCENDIARY_FIRE.get())).toList().isEmpty(), "impact left no incendiary fire");
            helper.setBlock(pos, Blocks.AIR.defaultBlockState());
            level.getServer().getPlayerList().remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "incendiary")
    public static void breakingTheCauldronRemovesItsBottles(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = new BlockPos(12, 2, 12);
        BlockPos at = helper.absolutePos(pos);
        helper.setBlock(pos, WildscapesBlocks.CAULDRON_OF_SOULS.get().defaultBlockState()
                .setValue(CauldronOfSoulsBlock.PHASE, CauldronOfSoulsBlock.Phase.ACTIVE));
        ServerPlayer player = survivalPlayer(helper);
        player.teleportTo(level, at.getX() + 6, at.getY(), at.getZ(), 0, 0);
        IncendiaryBottle bottle = new IncendiaryBottle(level, at.getX() + 0.5, at.getY() + 1.2, at.getZ() + 0.5);
        bottle.haunt(at, player);
        level.addFreshEntity(bottle);
        helper.runAfterDelay(15, () -> {
            helper.assertTrue(bottle.isAlive() && bottle.getY() > at.getY() + 1.6, "bottle did not float");
            helper.setBlock(pos, Blocks.AIR.defaultBlockState());
        });
        helper.runAfterDelay(17, () -> {
            helper.assertTrue(bottle.isRemoved(), "bottle survived its cauldron");
            level.getServer().getPlayerList().remove(player);
            helper.succeed();
        });
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

    private static ServerPlayer survivalPlayer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "bottle-test-player"), false);
        ServerPlayer player = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        return player;
    }
}
