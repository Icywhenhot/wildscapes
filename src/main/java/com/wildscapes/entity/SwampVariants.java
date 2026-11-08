package com.wildscapes.entity;

import com.mojang.serialization.Codec;
import com.wildscapes.Wildscapes;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.level.biome.Biome;

public final class SwampVariants {
    private SwampVariants() {}

    public static final TagKey<Biome> SWAMP_BIOMES = TagKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "swamp_variant_biomes"));

    private static final byte UNDECIDED = 0;
    private static final byte SWAMP = 1;
    private static final byte ELSEWHERE = 2;

    public static final AttachmentType<Byte> SWAMP_BORN = AttachmentRegistry.<Byte>builder()
            .persistent(Codec.BYTE)
            .syncWith(ByteBufCodecs.BYTE, AttachmentSyncPredicate.all())
            .buildAndRegister(Wildscapes.id("swamp_born"));

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register(SwampVariants::onEntityJoinLevel);
    }

    public static boolean isSwampBorn(Entity entity) {
        return entity.getAttachedOrElse(SWAMP_BORN, UNDECIDED) == SWAMP;
    }

    public static void onEntityJoinLevel(Entity entity, ServerLevel level) {
        if (hasSwampVariant(entity) && !entity.hasAttached(SWAMP_BORN)) {
            boolean swamp = level.getBiome(entity.blockPosition()).is(SWAMP_BIOMES);
            entity.setAttached(SWAMP_BORN, swamp ? SWAMP : ELSEWHERE);
        }
    }

    private static boolean hasSwampVariant(Entity entity) {
        return entity instanceof Pillager || entity instanceof Vindicator;
    }
}
