package com.wildscapes.entity;

import com.wildscapes.network.WildscapesNetwork;

import com.wildscapes.Wildscapes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = Wildscapes.MODID)
public final class SwampVariants {
    private SwampVariants() {}

    public static final TagKey<Biome> SWAMP_BIOMES = TagKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "swamp_variant_biomes"));

    public static final String SWAMP_BORN = "wildscapes:swamp_born";

    private static final byte UNDECIDED = 0;
    private static final byte SWAMP = 1;
    private static final byte ELSEWHERE = 2;

    public static boolean isSwampBorn(Entity entity) {
        return entity.getPersistentData().getByte(SWAMP_BORN) == SWAMP;
    }

    @SubscribeEvent
    static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (event.getLevel().isClientSide() || !hasSwampVariant(entity)) {
            return;
        }

        if (entity.getPersistentData().getByte(SWAMP_BORN) != UNDECIDED) {
            return;
        }
        boolean swamp = event.getLevel().getBiome(entity.blockPosition()).is(SWAMP_BIOMES);
        entity.getPersistentData().putByte(SWAMP_BORN, swamp ? SWAMP : ELSEWHERE);
        WildscapesNetwork.sync(entity);
    }

    private static boolean hasSwampVariant(Entity entity) {
        return entity instanceof Pillager || entity instanceof Vindicator;
    }
}
