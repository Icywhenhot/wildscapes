package com.wildscapes.particle;

import com.mojang.serialization.MapCodec;
import com.wildscapes.Wildscapes;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class WildscapesParticles {
    private WildscapesParticles() {}

    public static final SimpleParticleType MULTI_JUMP =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("multi_jump"), FabricParticleTypes.simple(true));

    public static final SimpleParticleType BREW_BUBBLE =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("brew_bubble"), FabricParticleTypes.simple(false));

    public static final SimpleParticleType INGREDIENT_STEAM =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("ingredient_steam"), FabricParticleTypes.simple(false));

    public static final SimpleParticleType RESIDUE_SPLAT =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("residue_splat"), FabricParticleTypes.simple(true));

    public static final SimpleParticleType RESIDUE_WISP =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("residue_wisp"), FabricParticleTypes.simple(false));

    public static final SimpleParticleType RESIDUE_SWIRL =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("residue_swirl"), FabricParticleTypes.simple(false));

    public static final SimpleParticleType SOUL_HARVEST_SPARK =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("soul_harvest_spark"), FabricParticleTypes.simple(false));

    public static final SimpleParticleType EMPOWERED_WISP =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("empowered_wisp"), FabricParticleTypes.simple(false));

    public static final SimpleParticleType EMPOWERED_RING =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("empowered_ring"), FabricParticleTypes.simple(false));

    public static final SimpleParticleType EMPOWERED_SOUL =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("empowered_soul"), FabricParticleTypes.simple(false));

    public static final SimpleParticleType SOUL_MOTE =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("soul_mote"), FabricParticleTypes.simple(false));

    public static final SimpleParticleType LOST_SOUL =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("lost_soul"), FabricParticleTypes.simple(false));

    public static final SimpleParticleType BONFIRE_EMBER =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("bonfire_ember"), FabricParticleTypes.simple(false));

    public static final SimpleParticleType CAULDRON_SWIRL =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("cauldron_swirl"), FabricParticleTypes.simple(false));

    public static final SimpleParticleType CAULDRON_REWARD =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("cauldron_reward"), FabricParticleTypes.simple(false));

    public static final SimpleParticleType SUMMON_WRAITH =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("summon_wraith"), FabricParticleTypes.simple(false));

    public static final SimpleParticleType SUMMON_RISE =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("summon_rise"), FabricParticleTypes.simple(false));

    public static final SimpleParticleType SUMMON_MOTE =
            Registry.register(BuiltInRegistries.PARTICLE_TYPE, Wildscapes.id("summon_mote"), FabricParticleTypes.simple(false));

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
