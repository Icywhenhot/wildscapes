package com.wildscapes.entity;

import com.wildscapes.WildscapesConfig;

import net.minecraft.world.entity.EntityType;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Bogged;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;

public final class SwampSpawns {
    private SwampSpawns() {}

    public static boolean check(EntityType<?> type, ServerLevelAccessor level, MobSpawnType reason,
            BlockPos pos, RandomSource random, boolean vanilla) {
        if (type == EntityType.BOGGED) {
            return vanilla && nightSwampBogged(level, reason, pos);
        }
        if (type == EntityType.DROWNED) {
            return vanilla || swampDrowned(level, reason, pos, random);
        }
        if (type == EntityType.SLIME) {
            return vanilla || daylightSwampSlime(level, reason, pos, random);
        }
        return vanilla;
    }

    private static boolean nightSwampBogged(ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos) {
        return spawnType != MobSpawnType.NATURAL || !isSwamp(level, pos) || level.getLevel().isNight();
    }

    private static boolean swampDrowned(ServerLevelAccessor level,
            MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (spawnType != MobSpawnType.NATURAL || !isSwamp(level, pos)) {
            return false;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }

        if (!level.getFluidState(pos).is(FluidTags.WATER)) {
            return false;
        }
        int rarity = WildscapesConfig.get(WildscapesConfig.DROWNED_RARITY);
        return rarity > 0 && Monster.isDarkEnoughToSpawn(level, pos, random) && random.nextInt(rarity) == 0;
    }

    private static boolean daylightSwampSlime(ServerLevelAccessor level,
            MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (spawnType != MobSpawnType.NATURAL || !isSwamp(level, pos)) {
            return false;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }

        if (!level.getLevel().isDay() || !level.canSeeSky(pos)) {
            return false;
        }
        int rarity = WildscapesConfig.get(WildscapesConfig.DAY_SLIME_RARITY);
        return rarity > 0 && random.nextInt(rarity) == 0
                && Mob.checkMobSpawnRules(EntityType.SLIME, level, spawnType, pos, random);
    }

    public static void sizeSlime(Slime slime, ServerLevelAccessor level, MobSpawnType reason) {
        BlockPos pos = slime.blockPosition();
        if (reason == MobSpawnType.NATURAL && level.getLevel().isDay()
                && level.canSeeSky(pos) && isSwamp(level, pos)) {
            slime.setSize(1, true);
        }
    }

    private static boolean isSwamp(LevelReader level, BlockPos pos) {
        return level.getBiome(pos).is(SwampVariants.SWAMP_BIOMES);
    }
}
