package com.wildscapes.particle;

import com.mojang.serialization.Codec;
import com.wildscapes.Wildscapes;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import com.wildscapes.registry.RegistryObject;
import com.wildscapes.registry.DeferredRegister;

public final class WildscapesParticles {
    private WildscapesParticles() {}

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Wildscapes.MODID);

    public static final RegistryObject<SimpleParticleType> MULTI_JUMP =
            PARTICLE_TYPES.register("multi_jump", () -> FabricParticleTypes.simple(true));

    public static final RegistryObject<SimpleParticleType> BREW_BUBBLE =
            PARTICLE_TYPES.register("brew_bubble", () -> FabricParticleTypes.simple(false));

    public static final RegistryObject<SimpleParticleType> INGREDIENT_STEAM =
            PARTICLE_TYPES.register("ingredient_steam", () -> FabricParticleTypes.simple(false));

    public static final RegistryObject<SimpleParticleType> RESIDUE_SPLAT =
            PARTICLE_TYPES.register("residue_splat", () -> FabricParticleTypes.simple(true));

    public static final RegistryObject<SimpleParticleType> RESIDUE_WISP =
            PARTICLE_TYPES.register("residue_wisp", () -> FabricParticleTypes.simple(false));

    public static final RegistryObject<SimpleParticleType> RESIDUE_SWIRL =
            PARTICLE_TYPES.register("residue_swirl", () -> FabricParticleTypes.simple(false));

    public static final RegistryObject<SimpleParticleType> SOUL_HARVEST_SPARK =
            PARTICLE_TYPES.register("soul_harvest_spark", () -> FabricParticleTypes.simple(false));

    public static final RegistryObject<SimpleParticleType> EMPOWERED_WISP =
            PARTICLE_TYPES.register("empowered_wisp", () -> FabricParticleTypes.simple(false));

    public static final RegistryObject<SimpleParticleType> EMPOWERED_RING =
            PARTICLE_TYPES.register("empowered_ring", () -> FabricParticleTypes.simple(false));

    public static final RegistryObject<SimpleParticleType> EMPOWERED_SOUL =
            PARTICLE_TYPES.register("empowered_soul", () -> FabricParticleTypes.simple(false));

    public static final RegistryObject<SimpleParticleType> SOUL_MOTE =
            PARTICLE_TYPES.register("soul_mote", () -> FabricParticleTypes.simple(false));

    public static final RegistryObject<SimpleParticleType> LOST_SOUL =
            PARTICLE_TYPES.register("lost_soul", () -> FabricParticleTypes.simple(false));

    public static final RegistryObject<SimpleParticleType> BONFIRE_EMBER =
            PARTICLE_TYPES.register("bonfire_ember", () -> FabricParticleTypes.simple(false));

    public static final RegistryObject<SimpleParticleType> CAULDRON_SWIRL =
            PARTICLE_TYPES.register("cauldron_swirl", () -> FabricParticleTypes.simple(false));

    public static final RegistryObject<SimpleParticleType> CAULDRON_REWARD =
            PARTICLE_TYPES.register("cauldron_reward", () -> FabricParticleTypes.simple(false));

    public static final RegistryObject<SimpleParticleType> SUMMON_WRAITH =
            PARTICLE_TYPES.register("summon_wraith", () -> FabricParticleTypes.simple(false));

    public static final RegistryObject<SimpleParticleType> SUMMON_RISE =
            PARTICLE_TYPES.register("summon_rise", () -> FabricParticleTypes.simple(false));

    public static final RegistryObject<SimpleParticleType> SUMMON_MOTE =
            PARTICLE_TYPES.register("summon_mote", () -> FabricParticleTypes.simple(false));

    public static final RegistryObject<ParticleType<SoulTrailOptions>> SOUL_TRAIL =
            PARTICLE_TYPES.register("soul_trail", () -> new ParticleType<SoulTrailOptions>(true, SoulTrailOptions.DESERIALIZER) {
                @Override
                public Codec<SoulTrailOptions> codec() {
                    return SoulTrailOptions.CODEC;
                }

            });

    public static void register() {
        PARTICLE_TYPES.register();
    }
}
