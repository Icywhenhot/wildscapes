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

/**
 * Tracks which vanilla mobs were born in a swamp so the client can give them their
 * swamp-only look. The answer is baked in once, at the spot the mob first appeared,
 * and then travels with it — a pillager patrol that wanders out of the marsh keeps
 * the mossy robes it grew up in.
 *
 * <p>Mobs that always get a redesign regardless of biome (witches, slimes) don't go
 * through here at all; their renderers simply never look at vanilla's textures.
 */
@EventBusSubscriber(modid = Wildscapes.MODID)
public final class SwampVariants {
    private SwampVariants() {}

    /** Biomes whose pillagers and vindicators wear the swamp redesign. */
    public static final TagKey<Biome> SWAMP_BIOMES = TagKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "swamp_variant_biomes"));

    private static final byte UNDECIDED = 0;
    private static final byte SWAMP = 1;
    private static final byte ELSEWHERE = 2;

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Wildscapes.MODID);

    /**
     * Tri-state so we can tell "spawned outside a swamp" apart from "not looked at yet":
     * the flag is decided once and then persists, instead of being re-derived from
     * wherever the mob happens to be standing when its chunk reloads.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Byte>> SWAMP_BORN =
            ATTACHMENT_TYPES.register("swamp_born", () -> AttachmentType.<Byte>builder(() -> UNDECIDED)
                    .serialize(Codec.BYTE)
                    .sync(ByteBufCodecs.BYTE)
                    .build());

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }

    /** Whether this entity should be rendered with its swamp redesign. */
    public static boolean isSwampBorn(Entity entity) {
        return entity.getData(SWAMP_BORN) == SWAMP;
    }

    @SubscribeEvent
    static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (event.getLevel().isClientSide() || !hasSwampVariant(entity)) {
            return;
        }
        // Fires for freshly spawned mobs *and* for ones being read back off disk, so only
        // mobs that have never been judged get judged — the rest keep what they were born with.
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
