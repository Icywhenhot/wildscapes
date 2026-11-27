package com.wildscapes.particle;

import com.mojang.serialization.MapCodec;
import com.wildscapes.Wildscapes;

import net.minecraft.core.Registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class WildscapesParticles {
    private WildscapesParticles() {}

    public static final SimpleParticleType MULTI_JUMP =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("multi_jump"), new SimpleParticleType(true));

    public static final SimpleParticleType BREW_BUBBLE =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("brew_bubble"), new SimpleParticleType(false));

    public static final SimpleParticleType INGREDIENT_STEAM =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("ingredient_steam"), new SimpleParticleType(false));

    public static final SimpleParticleType RESIDUE_SPLAT =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("residue_splat"), new SimpleParticleType(true));

    public static final SimpleParticleType RESIDUE_WISP =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("residue_wisp"), new SimpleParticleType(false));

    public static final SimpleParticleType RESIDUE_SWIRL =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("residue_swirl"), new SimpleParticleType(false));

    public static final SimpleParticleType SOUL_HARVEST_SPARK =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("soul_harvest_spark"), new SimpleParticleType(false));

    public static final SimpleParticleType EMPOWERED_WISP =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("empowered_wisp"), new SimpleParticleType(false));

    public static final SimpleParticleType EMPOWERED_RING =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("empowered_ring"), new SimpleParticleType(false));

    public static final SimpleParticleType EMPOWERED_SOUL =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("empowered_soul"), new SimpleParticleType(false));

    public static final SimpleParticleType SOUL_MOTE =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("soul_mote"), new SimpleParticleType(false));

    public static final SimpleParticleType LOST_SOUL =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("lost_soul"), new SimpleParticleType(false));

    public static final SimpleParticleType BONFIRE_EMBER =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("bonfire_ember"), new SimpleParticleType(false));

    public static final SimpleParticleType CAULDRON_SWIRL =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("cauldron_swirl"), new SimpleParticleType(false));

    public static final SimpleParticleType CAULDRON_REWARD =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("cauldron_reward"), new SimpleParticleType(false));

    public static final SimpleParticleType SUMMON_WRAITH =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("summon_wraith"), new SimpleParticleType(false));

    public static final SimpleParticleType SUMMON_RISE =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("summon_rise"), new SimpleParticleType(false));

    public static final SimpleParticleType SUMMON_MOTE =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("summon_mote"), new SimpleParticleType(false));

    public static final ParticleType<SoulTrailOptions> SOUL_TRAIL =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("soul_trail"), new ParticleType<SoulTrailOptions>(true) {
                @Override
                public MapCodec<SoulTrailOptions> codec() {
                    return SoulTrailOptions.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, SoulTrailOptions> streamCodec() {
                    return SoulTrailOptions.STREAM_CODEC;
                }
            });

    public static void register() {}
}
