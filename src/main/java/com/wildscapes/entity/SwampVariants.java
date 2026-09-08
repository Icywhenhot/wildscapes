package com.wildscapes.entity;

import com.mojang.serialization.Codec;
import com.wildscapes.Wildscapes;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@EventBusSubscriber(modid = Wildscapes.MODID)
public final class SwampVariants {
    private SwampVariants() {}

    public static final TagKey<Biome> SWAMP_BIOMES = TagKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "swamp_variant_biomes"));

    private static final byte UNDECIDED = 0;
    private static final byte SWAMP = 1;
    private static final byte ELSEWHERE = 2;

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Wildscapes.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Byte>> SWAMP_BORN =
            ATTACHMENT_TYPES.register("swamp_born", () -> AttachmentType.<Byte>builder(() -> UNDECIDED)
                    .serialize(Codec.BYTE)
                    .sync(ByteBufCodecs.BYTE)
                    .build());

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }

    public static boolean isSwampBorn(Entity entity) {
        return entity.getData(SWAMP_BORN) == SWAMP;
    }

    @SubscribeEvent
    static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (event.getLevel().isClientSide() || !hasSwampVariant(entity)) {
            return;
        }

        if (entity.getData(SWAMP_BORN) != UNDECIDED) {
            return;
        }
        boolean swamp = event.getLevel().getBiome(entity.blockPosition()).is(SWAMP_BIOMES);
        entity.setData(SWAMP_BORN, swamp ? SWAMP : ELSEWHERE);
    }

    private static boolean hasSwampVariant(Entity entity) {
        return entity instanceof Pillager || entity instanceof Vindicator;
    }
}
