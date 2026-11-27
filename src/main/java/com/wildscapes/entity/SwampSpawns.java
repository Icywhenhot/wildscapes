package com.wildscapes.entity;

import com.wildscapes.Backports;
import com.wildscapes.Wildscapes;
import com.wildscapes.WildscapesConfig;

import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import com.wildscapes.entity.Bogged;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;

public final class SwampSpawns {
    private SwampSpawns() {}

    @SuppressWarnings("unchecked")
    public static void registerSpawnPlacements() {
        SpawnPlacements.register(WildscapesEntities.BOGGED.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> Monster.checkMonsterSpawnRules(type, level, reason, pos, random)
                        && nightSwampBogged(type, level, reason, pos, random));
    }

    @SuppressWarnings("unchecked")
    public static boolean check(EntityType<?> type, ServerLevelAccessor level, MobSpawnType reason,
            BlockPos pos, RandomSource random, boolean vanilla) {
        if (type == EntityType.DROWNED) {
            return vanilla || swampDrowned((EntityType<Drowned>) type, level, reason, pos, random);
        }
        if (type == EntityType.SLIME) {
            return vanilla || daylightSwampSlime((EntityType<Slime>) type, level, reason, pos, random);
        }
        return vanilla;
    }

    private static boolean nightSwampBogged(EntityType<Bogged> type, ServerLevelAccessor level,
            MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (spawnType == MobSpawnType.NATURAL && Backports.hasBogged()) {
            return false;
        }
        return spawnType != MobSpawnType.NATURAL || !isSwamp(level, pos) || level.getLevel().isNight();
    }

    private static boolean swampDrowned(EntityType<Drowned> type, ServerLevelAccessor level,
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

    private static boolean daylightSwampSlime(EntityType<Slime> type, ServerLevelAccessor level,
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
                && Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    public static boolean keepsSpawnSize(Mob mob, ServerLevelAccessor level, MobSpawnType reason) {
        if (!(mob instanceof Slime slime) || slime instanceof MagmaCube) {
            return true;
        }
        BlockPos pos = slime.blockPosition();
        if (reason != MobSpawnType.NATURAL
                || !level.getLevel().isDay()
                || !level.canSeeSky(pos)
                || !isSwamp(level, pos)) {
            return true;
        }
        slime.setSize(1, true);
        return false;
    }

    private static boolean isSwamp(LevelReader level, BlockPos pos) {
        return level.getBiome(pos).is(SwampVariants.SWAMP_BIOMES);
    }
}
