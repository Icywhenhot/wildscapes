package com.wildscapes.particle;

import com.wildscapes.Wildscapes;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
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

    public static void register(IEventBus bus) {
        PARTICLE_TYPES.register(bus);
    }
}
