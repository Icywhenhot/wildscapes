package com.wildscapes.entity;

import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.attachment.AttachmentType;
import com.mojang.serialization.Codec;
import com.wildscapes.Wildscapes;

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

    public static final AttachmentType<Byte> SWAMP_BORN = AttachmentType.builder(() -> UNDECIDED)
            .serialize(Codec.BYTE.fieldOf("value"))
            .sync(ByteBufCodecs.BYTE)
            .build();

    public static void registerAttachments() {
        Registry.register(NeoForgeRegistries.ATTACHMENT_TYPES, Wildscapes.id("swamp_born"), SWAMP_BORN);
    }

    public static boolean isSwampBorn(Entity entity) {
        return entity.getExistingData(SWAMP_BORN).orElse(UNDECIDED) == SWAMP;
    }

    public static void onEntityJoinLevel(Entity entity, ServerLevel level) {
        if (hasSwampVariant(entity) && !entity.hasData(SWAMP_BORN)) {
            boolean swamp = level.getBiome(entity.blockPosition()).is(SWAMP_BIOMES);
            entity.setData(SWAMP_BORN, swamp ? SWAMP : ELSEWHERE);
        }
    }

    private static boolean hasSwampVariant(Entity entity) {
        return entity instanceof Pillager || entity instanceof Vindicator;
    }
}
