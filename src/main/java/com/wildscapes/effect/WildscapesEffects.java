package com.wildscapes.effect;

import com.wildscapes.Wildscapes;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public final class WildscapesEffects {
    private WildscapesEffects() {}

    public static final Holder.Reference<MobEffect> MIRAGE = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Wildscapes.id("mirage"), new MobEffect(MobEffectCategory.BENEFICIAL, 0x6E5FCD) {});

    public static final Holder.Reference<MobEffect> SLIMEBOUND = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Wildscapes.id("slimebound"), new SlimeboundEffect());

    public static final Holder.Reference<MobEffect> INTANGIBILITY = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Wildscapes.id("intangibility"), new MobEffect(MobEffectCategory.BENEFICIAL, 0x7795C4) {});

    public static final Holder.Reference<MobEffect> GNASHING = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Wildscapes.id("gnashing"), new GnashingEffect());

    public static final Holder.Reference<MobEffect> INCENDIARY = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Wildscapes.id("incendiary"), new IncendiaryEffect());

    public static final Holder.Reference<MobEffect> SOUL_HARVEST = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Wildscapes.id("soul_harvest"), new MobEffect(MobEffectCategory.NEUTRAL, 0xC2336F) {});

    public static void register() {}
}
