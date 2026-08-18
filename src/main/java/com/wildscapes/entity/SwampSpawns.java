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

/**
 * Two additions to what a swamp puts out at you.
 *
 * <p><b>Drowned</b> wade in the shallows. Vanilla only lets them spawn deep — five blocks below
 * sea level — which no swamp pool ever is, so they are unheard of there; this drops the depth
 * requirement for swamps and leaves the rest of vanilla's rule (night, standing in water) alone.
 *
 * <p><b>Slimes</b> get to turn up in broad daylight, which vanilla forbids outside slime chunks:
 * its surface rule wants darkness and a bright moon. A daylight one is always a small slime —
 * see {@link #onFinalizeSpawn} — so running into one is a nuisance rather than a fight.
 *
 * <p>Both are deliberately thinned down by a dice roll, since the spawner tries far more often
 * than a player would guess; the two rarity constants below are the dials for that.
 */
@EventBusSubscriber(modid = Wildscapes.MODID)
public final class SwampSpawns {
    private SwampSpawns() {}

    /** One in this many otherwise-suitable water spots actually produces a drowned. */
    private static final int DROWNED_RARITY = 20;

    /** One in this many otherwise-suitable daylight spots actually produces a slime. */
    private static final int DAY_SLIME_RARITY = 100;

    /** Registered by hand from the mod constructor: this one rides the mod bus, not the game bus. */
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        // OR: vanilla's own rules still apply everywhere else, these are extra chances on top.
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
        // Standing in water, with water under it — as vanilla asks, just not five blocks deep.
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
        // Out under the open sky in daylight; slime chunks underground stay vanilla's business.
        if (!level.getLevel().isDay() || !level.canSeeSky(pos)) {
            return false;
        }
        return random.nextInt(DAY_SLIME_RARITY) == 0
                && Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    /**
     * Keeps daylight slimes small. Size is rolled inside {@link Slime#finalizeSpawn}, so the way
     * to pin it down is to skip that call and set the size here instead — all a slime loses with
     * it is the small random tweak vanilla makes to a mob's follow range.
     */
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
