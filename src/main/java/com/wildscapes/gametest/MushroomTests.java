package com.wildscapes.gametest;

import com.wildscapes.block.ShelfMushroomBlock;
import com.wildscapes.block.PavShrooms;
import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.entity.SwampVariants;
import com.wildscapes.worldgen.WildscapesFeatures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.util.RandomSource;
import java.util.Optional;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wildscapes")
@PrefixGameTestTemplate(false)
public class MushroomTests {
    @GameTest(template = "gametest/tree_platform")
    public static void pavShroomsShareAnItemAndDropByHand(GameTestHelper helper) {
        var level = helper.getLevel();
        var ground = WildscapesBlocks.LUMINOUS_MUSHROOM.get();
        var shelf = WildscapesBlocks.SHELF_LUMINOUS_MUSHROOM.get();
        var item = ground.asItem();
        helper.assertTrue(shelf.asItem() == item, "log mushroom has a separate item");
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item, 8));
        BlockPos soil = new BlockPos(5, 2, 5);
        helper.setBlock(soil, Blocks.MUD);
        helper.setBlock(soil.above(), Blocks.AIR);
        BlockPos absolute = helper.absolutePos(soil);
        var hit = new BlockHitResult(Vec3.atCenterOf(absolute).add(0, 0.5, 0), Direction.UP, absolute, false);
        var context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, player.getMainHandItem(), hit);
        helper.assertTrue(((BlockItem) item).place(context).consumesAction(), "ground placement failed");
        helper.assertBlockPresent(ground, soil.above());
        BlockPos trunk = new BlockPos(10, 4, 10);
        helper.setBlock(trunk, Blocks.OAK_LOG);
        for (Direction face : Direction.Plane.HORIZONTAL) {
            BlockPos pos = trunk.relative(face);
            helper.setBlock(pos, Blocks.AIR);
            absolute = helper.absolutePos(trunk);
            hit = new BlockHitResult(Vec3.atCenterOf(absolute).relative(face, 0.5), face, absolute, false);
            context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, player.getMainHandItem(), hit);
            helper.assertTrue(((BlockItem) item).place(context).consumesAction(), "log placement failed on " + face);
            helper.assertBlockPresent(shelf, pos);
            helper.assertTrue(helper.getBlockState(pos).getValue(ShelfMushroomBlock.FACING) == face,
                    "log mushroom faces the wrong way");
        }
        for (BlockPos pos : java.util.List.of(soil.above(), trunk.north())) {
            var state = helper.getBlockState(pos);
            var drops = Block.getDrops(state, level, helper.absolutePos(pos), null, player, ItemStack.EMPTY);
            helper.assertTrue(!state.requiresCorrectToolForDrops() && state.getDestroySpeed(level, helper.absolutePos(pos)) == 0,
                    "mushroom requires a tool or is not instant to break");
            helper.assertTrue(drops.size() == 1 && drops.getFirst().is(item) && drops.getFirst().getCount() == 1,
                    "bare-hand harvest did not drop one pav shroom");
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/tree_platform")
    public static void pavShroomsHealWithinThreeBlocks(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        BlockPos pos = new BlockPos(10, 4, 10);
        helper.setBlock(pos.below(), Blocks.MUD);
        helper.setBlock(pos.south(), WildscapesBlocks.CYPRESS_LOG.get());
        Vec3 center = Vec3.atCenterOf(helper.absolutePos(pos));
        for (var mushroom : java.util.List.of(WildscapesBlocks.LUMINOUS_MUSHROOM.get().defaultBlockState(),
                WildscapesBlocks.SHELF_LUMINOUS_MUSHROOM.get().defaultBlockState())) {
            helper.setBlock(pos, mushroom);
            player.setHealth(10.0F);
            player.setPos(center.add(2.9, 0, 0));
            player.tickCount = 49;
            PavShrooms.onTick(new PlayerTickEvent.Post(player));
            helper.assertTrue(player.getHealth() == 10.0F, "pav shroom healed between pulses");
            player.tickCount = 50;
            PavShrooms.onTick(new PlayerTickEvent.Post(player));
            helper.assertTrue(player.getHealth() == 10.5F, "pav shroom did not heal half a health point");
            player.setPos(center.add(3.01, 0, 0));
            player.tickCount = 100;
            PavShrooms.onTick(new PlayerTickEvent.Post(player));
            helper.assertTrue(player.getHealth() == 10.5F, "pav shroom healed outside three blocks");
            player.setPos(center.add(2.9, 0, 2.9));
            PavShrooms.onTick(new PlayerTickEvent.Post(player));
            helper.assertTrue(player.getHealth() == 10.5F, "pav shroom aura used a cube instead of a radius");
        }
        helper.setBlock(pos.east().below(), Blocks.MUD);
        helper.setBlock(pos.east(), WildscapesBlocks.LUMINOUS_MUSHROOM.get());
        player.setPos(center);
        player.setHealth(10.0F);
        PavShrooms.onTick(new PlayerTickEvent.Post(player));
        helper.assertTrue(player.getHealth() == 10.5F, "overlapping pav shrooms stacked healing");
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200));
        PavShrooms.onTick(new PlayerTickEvent.Post(player));
        helper.assertTrue(player.getHealth() == 10.5F && player.hasEffect(MobEffects.REGENERATION),
                "pav shroom changed potion regeneration");
        player.removeEffect(MobEffects.REGENERATION);
        player.setHealth(10.0F);
        MobEffects.REGENERATION.value().applyEffectTick(player, 0);
        helper.assertTrue(player.getHealth() == 11.0F, "pav shroom healing was not half vanilla regeneration");
        helper.succeed();
    }

    @GameTest(template = "gametest/tree_platform")
    public static void shelfMushroomsStayNearTheGround(GameTestHelper helper) {
        helper.setBiome(Biomes.SWAMP);
        for (int x = 7; x <= 13; x++) {
            for (int z = 7; z <= 13; z++) {
                helper.setBlock(new BlockPos(x, 2, z), Blocks.MUD);
                for (int y = 3; y <= 24; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
            }
        }
        for (int y = 3; y <= 12; y++) helper.setBlock(new BlockPos(10, y, 10), WildscapesBlocks.CYPRESS_LOG.get());
        var level = helper.getLevel();
        var config = new SimpleBlockConfiguration(BlockStateProvider.simple(WildscapesBlocks.SHELF_LUMINOUS_MUSHROOM.get()));
        var random = RandomSource.create(1234);
        BlockPos beside = helper.absolutePos(new BlockPos(9, 3, 10));
        helper.assertTrue(level.getBiome(beside).is(Biomes.SWAMP), "test biome is not swamp at " + beside);
        helper.assertTrue(level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                beside.getX(), beside.getZ()) == beside.getY(), "test surface is at "
                + level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, beside.getX(), beside.getZ())
                + " instead of " + beside.getY());
        int low = 0;
        int high = 0;
        for (int i = 0; i < 160; i++) {
            boolean placed = WildscapesFeatures.SHELF_MUSHROOM.get().place(new FeaturePlaceContext<>(Optional.empty(), level,
                    level.getChunkSource().getGenerator(), random, helper.absolutePos(new BlockPos(10, 3, 10)), config));
            if (!placed) continue;
            for (Direction face : Direction.Plane.HORIZONTAL) {
                for (int y = 3; y <= 12; y++) {
                    BlockPos pos = new BlockPos(10, y, 10).relative(face);
                    if (!helper.getBlockState(pos).is(WildscapesBlocks.SHELF_LUMINOUS_MUSHROOM.get())) continue;
                    helper.assertTrue(y <= 6, "mushroom grew too high on the trunk");
                    if (y <= 4) low++; else high++;
                    helper.setBlock(pos, Blocks.AIR);
                }
            }
        }
        helper.assertTrue(low > 30 && low > high * 2, "mushrooms did not favor the lower trunk: " + low + "/" + high);
        helper.setBiome(Biomes.MANGROVE_SWAMP);
        helper.assertTrue(!WildscapesFeatures.SHELF_MUSHROOM.get().place(new FeaturePlaceContext<>(Optional.empty(), level,
                level.getChunkSource().getGenerator(), random, helper.absolutePos(new BlockPos(10, 3, 10)), config)),
                "shelf mushroom generated in mangrove swamp");
        helper.succeed();
    }

    @GameTest(template = "gametest/tree_platform")
    public static void groundMushroomNeedsSoil(GameTestHelper helper) {
        BlockPos pos = new BlockPos(5, 3, 5);
        var state = WildscapesBlocks.LUMINOUS_MUSHROOM.get().defaultBlockState();
        for (var soil : new net.minecraft.world.level.block.Block[] {Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.MUD, Blocks.CLAY}) {
            helper.setBlock(pos.below(), soil);
            helper.assertTrue(state.canSurvive(helper.getLevel(), helper.absolutePos(pos)), "mushroom rejected " + soil);
        }
        helper.setBlock(pos.below(), Blocks.STONE);
        helper.assertTrue(!state.canSurvive(helper.getLevel(), helper.absolutePos(pos)), "mushroom accepted stone");
        helper.setBlock(pos.below(), Blocks.MUD);
        helper.setBlock(pos, state);
        helper.assertTrue(state.getLightEmission(helper.getLevel(), helper.absolutePos(pos)) == 7, "ground mushroom light is not 7");
        helper.succeed();
    }

    @GameTest(template = "gametest/tree_platform")
    public static void shelfMushroomNeedsATrunk(GameTestHelper helper) {
        BlockPos trunk = new BlockPos(10, 4, 10);
        helper.setBlock(trunk, WildscapesBlocks.CYPRESS_LOG.get());
        for (Direction face : Direction.Plane.HORIZONTAL) {
            BlockPos pos = trunk.relative(face);
            var state = WildscapesBlocks.SHELF_LUMINOUS_MUSHROOM.get().defaultBlockState().setValue(ShelfMushroomBlock.FACING, face);
            helper.assertTrue(state.canSurvive(helper.getLevel(), helper.absolutePos(pos)), "mushroom rejected trunk face " + face);
            helper.setBlock(pos, state);
            helper.assertTrue(state.getLightEmission(helper.getLevel(), helper.absolutePos(pos)) == 7, "shelf mushroom light is not 7");
        }
        helper.setBlock(trunk, Blocks.AIR);
        helper.succeedWhen(() -> {
            for (Direction face : Direction.Plane.HORIZONTAL) helper.assertBlockPresent(Blocks.AIR, trunk.relative(face));
        });
    }

    @GameTest(template = "gametest/tree_platform")
    public static void mangroveKeepsVanillaGeneration(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        var swamp = biomes.getHolderOrThrow(Biomes.SWAMP);
        var mangrove = biomes.getHolderOrThrow(Biomes.MANGROVE_SWAMP);
        helper.assertTrue(swamp.is(SwampVariants.SWAMP_BIOMES), "swamp is missing from biome tag");
        helper.assertTrue(!mangrove.is(SwampVariants.SWAMP_BIOMES), "mangrove is in biome tag");
        for (var step : mangrove.value().getGenerationSettings().features()) {
            for (var feature : step) {
                helper.assertTrue(feature.unwrapKey().orElseThrow().location().getNamespace().equals("minecraft"),
                        "mangrove contains a modded feature: " + feature);
            }
        }
        var structures = helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (var entry : structures.entrySet()) {
            if (entry.getKey().location().getNamespace().equals("wildscapes")) {
                helper.assertTrue(entry.getValue().biomes().contains(swamp), "structure excludes swamp: " + entry.getKey());
                helper.assertTrue(!entry.getValue().biomes().contains(mangrove), "structure includes mangrove: " + entry.getKey());
            }
        }
        helper.succeed();
    }
}
