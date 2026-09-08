package com.wildscapes.effect;

import com.wildscapes.Wildscapes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WildscapesEffects {
    private WildscapesEffects() {}

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Wildscapes.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> MIRAGE = EFFECTS.register("mirage",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x6E5FCD) {});

    public static final DeferredHolder<MobEffect, MobEffect> SLIMEBOUND = EFFECTS.register("slimebound",
            SlimeboundEffect::new);

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}
