package com.wildscapes.entity;

import net.minecraft.server.level.ServerLevel;
import com.wildscapes.network.WildscapesNetwork;

import com.wildscapes.Wildscapes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.level.biome.Biome;

public final class SwampVariants {
    private SwampVariants() {}

    public static final TagKey<Biome> SWAMP_BIOMES = TagKey.create(Registries.BIOME,
            new ResourceLocation(Wildscapes.MODID, "swamp_variant_biomes"));

    public static final String SWAMP_BORN = "wildscapes:swamp_born";

    private static final byte UNDECIDED = 0;
    private static final byte SWAMP = 1;
    private static final byte ELSEWHERE = 2;

    public static boolean isSwampBorn(Entity entity) {
        return WildscapesEntityData.of(entity).getByte(SWAMP_BORN) == SWAMP;
    }

    public static void onEntityJoinLevel(Entity entity, ServerLevel level) {
        if (!hasSwampVariant(entity)) {
            return;
        }

        if (WildscapesEntityData.of(entity).getByte(SWAMP_BORN) != UNDECIDED) {
            return;
        }
        boolean swamp = level.getBiome(entity.blockPosition()).is(SWAMP_BIOMES);
        WildscapesEntityData.of(entity).putByte(SWAMP_BORN, swamp ? SWAMP : ELSEWHERE);
        WildscapesNetwork.sync(entity);
    }

    private static boolean hasSwampVariant(Entity entity) {
        return entity instanceof Pillager || entity instanceof Vindicator;
    }
}
