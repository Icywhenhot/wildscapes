package com.wildscapes.particle;

import com.wildscapes.Wildscapes;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Particles for the brewing cauldron. Both are plain sprite-sheet particles whose frames live in
 * {@code assets/wildscapes/particles/}; their client-side providers are hooked up in
 * {@link com.wildscapes.WildscapesClient}.
 */
public final class WildscapesParticles {
    private WildscapesParticles() {}

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Wildscapes.MODID);

    /** Big lazy bubbles that boil up when nether wart turns the water into a soup base. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BREW_BUBBLE =
            PARTICLE_TYPES.register("brew_bubble", () -> new SimpleParticleType(false));

    /** The puff that steams up out of the pot while an effect ingredient reacts with the brew. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> INGREDIENT_STEAM =
            PARTICLE_TYPES.register("ingredient_steam", () -> new SimpleParticleType(false));

    public static void register(IEventBus bus) {
        PARTICLE_TYPES.register(bus);
    }
}
