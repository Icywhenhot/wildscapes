package com.wildscapes.entity;

import net.minecraft.world.entity.EntityTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.cubemob.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;

public final class SwampSpawns {
    private SwampSpawns() {}

    private static final int DROWNED_RARITY = 20;

    private static final int DAY_SLIME_RARITY = 100;

    public static boolean check(EntityType<?> type, ServerLevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random, boolean vanilla) {
        if (type == EntityTypes.BOGGED) {
            return vanilla && nightSwampBogged(level, reason, pos);
        }
        if (type == EntityTypes.DROWNED) {
            return vanilla || swampDrowned(level, reason, pos, random);
        }
        if (type == EntityTypes.SLIME) {
            return vanilla || daylightSwampSlime(level, reason, pos, random);
        }
        return vanilla;
    }

    private static boolean nightSwampBogged(ServerLevelAccessor level, EntitySpawnReason spawnType, BlockPos pos) {
        return spawnType != EntitySpawnReason.NATURAL || !isSwamp(level, pos) || level.getLevel().isDarkOutside();
    }

    private static boolean swampDrowned(ServerLevelAccessor level,
            EntitySpawnReason spawnType, BlockPos pos, RandomSource random) {
        if (spawnType != EntitySpawnReason.NATURAL || !isSwamp(level, pos)) {
            return false;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }

        if (!level.getFluidState(pos).is(FluidTags.WATER)) {
            return false;
        }
        return Monster.isDarkEnoughToSpawn(level, pos, random) && random.nextInt(DROWNED_RARITY) == 0;
    }

    private static boolean daylightSwampSlime(ServerLevelAccessor level,
            EntitySpawnReason spawnType, BlockPos pos, RandomSource random) {
        if (spawnType != EntitySpawnReason.NATURAL || !isSwamp(level, pos)) {
            return false;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }

        if (!level.getLevel().isBrightOutside() || !level.canSeeSky(pos)) {
            return false;
        }
        return random.nextInt(DAY_SLIME_RARITY) == 0
                && Mob.checkMobSpawnRules(EntityTypes.SLIME, level, spawnType, pos, random);
    }

    public static void sizeSlime(Slime slime, ServerLevelAccessor level, EntitySpawnReason reason) {
        BlockPos pos = slime.blockPosition();
        if (reason == EntitySpawnReason.NATURAL && level.getLevel().isBrightOutside()
                && level.canSeeSky(pos) && isSwamp(level, pos)) {
            slime.setSize(1, true);
        }
    }

    private static boolean isSwamp(LevelReader level, BlockPos pos) {
        return level.getBiome(pos).is(SwampVariants.SWAMP_BIOMES);
    }
}
