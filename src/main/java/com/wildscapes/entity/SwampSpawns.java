package com.wildscapes.entity;

import com.wildscapes.Wildscapes;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

@EventBusSubscriber(modid = Wildscapes.MODID)
public final class SwampSpawns {
    private SwampSpawns() {}

    private static final int DROWNED_RARITY = 20;

    private static final int DAY_SLIME_RARITY = 100;

    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(EntityType.DROWNED, SwampSpawns::swampDrowned,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityType.SLIME, SwampSpawns::daylightSwampSlime,
                RegisterSpawnPlacementsEvent.Operation.OR);
    }

    private static boolean swampDrowned(EntityType<Drowned> type, ServerLevelAccessor level,
            MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (spawnType != MobSpawnType.NATURAL || !isSwamp(level, pos)) {
            return false;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }

        if (!level.getFluidState(pos).is(FluidTags.WATER) || !level.getFluidState(pos.below()).is(FluidTags.WATER)) {
            return false;
        }
        return Monster.isDarkEnoughToSpawn(level, pos, random) && random.nextInt(DROWNED_RARITY) == 0;
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
        return random.nextInt(DAY_SLIME_RARITY) == 0
                && Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    @SubscribeEvent
    static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!(event.getEntity() instanceof Slime slime) || slime instanceof MagmaCube) {
            return;
        }
        ServerLevelAccessor level = event.getLevel();
        BlockPos pos = slime.blockPosition();
        if (event.getSpawnType() != MobSpawnType.NATURAL
                || !level.getLevel().isDay()
                || !level.canSeeSky(pos)
                || !isSwamp(level, pos)) {
            return;
        }
        event.setCanceled(true);
        slime.setSize(1, true);
    }

    private static boolean isSwamp(LevelReader level, BlockPos pos) {
        return level.getBiome(pos).is(SwampVariants.SWAMP_BIOMES);
    }
}
