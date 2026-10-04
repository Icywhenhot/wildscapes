package com.wildscapes.particle;

import com.mojang.serialization.MapCodec;
import com.wildscapes.Wildscapes;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WildscapesParticles {
    private WildscapesParticles() {}

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Wildscapes.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BREW_BUBBLE =
            PARTICLE_TYPES.register("brew_bubble", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> INGREDIENT_STEAM =
            PARTICLE_TYPES.register("ingredient_steam", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RESIDUE_SPLAT =
            PARTICLE_TYPES.register("residue_splat", () -> new SimpleParticleType(true));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RESIDUE_WISP =
            PARTICLE_TYPES.register("residue_wisp", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RESIDUE_SWIRL =
            PARTICLE_TYPES.register("residue_swirl", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> INCURSION_SPARK =
            PARTICLE_TYPES.register("incursion_spark", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> EMPOWERED_WISP =
            PARTICLE_TYPES.register("empowered_wisp", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> EMPOWERED_RING =
            PARTICLE_TYPES.register("empowered_ring", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> EMPOWERED_SOUL =
            PARTICLE_TYPES.register("empowered_soul", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SOUL_MOTE =
            PARTICLE_TYPES.register("soul_mote", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LOST_SOUL =
            PARTICLE_TYPES.register("lost_soul", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BONFIRE_EMBER =
            PARTICLE_TYPES.register("bonfire_ember", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CAULDRON_SWIRL =
            PARTICLE_TYPES.register("cauldron_swirl", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CAULDRON_REWARD =
            PARTICLE_TYPES.register("cauldron_reward", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SUMMON_WRAITH =
            PARTICLE_TYPES.register("summon_wraith", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SUMMON_RISE =
            PARTICLE_TYPES.register("summon_rise", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SUMMON_MOTE =
            PARTICLE_TYPES.register("summon_mote", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, ParticleType<SoulTrailOptions>> SOUL_TRAIL =
            PARTICLE_TYPES.register("soul_trail", () -> new ParticleType<SoulTrailOptions>(true) {
                @Override
                public MapCodec<SoulTrailOptions> codec() {
                    return SoulTrailOptions.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, SoulTrailOptions> streamCodec() {
                    return SoulTrailOptions.STREAM_CODEC;
                }
            });

    public static void register(IEventBus bus) {
        PARTICLE_TYPES.register(bus);
    }
}
