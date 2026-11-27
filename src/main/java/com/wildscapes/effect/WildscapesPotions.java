package com.wildscapes.effect;

import com.wildscapes.Wildscapes;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;

public final class WildscapesPotions {
    private WildscapesPotions() {}

    public static final Holder.Reference<Potion> MIRAGE = Registry.registerForHolder(BuiltInRegistries.POTION, Wildscapes.id("mirage"), new Potion("mirage", new MobEffectInstance(WildscapesEffects.MIRAGE, 3600)));

    public static final Holder.Reference<Potion> LONG_MIRAGE = Registry.registerForHolder(BuiltInRegistries.POTION, Wildscapes.id("long_mirage"), new Potion("mirage", new MobEffectInstance(WildscapesEffects.MIRAGE, 9600)));

    public static final Holder.Reference<Potion> RESISTANCE = Registry.registerForHolder(BuiltInRegistries.POTION, Wildscapes.id("resistance"), new Potion("resistance", new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200)));

    public static final Holder.Reference<Potion> LONG_RESISTANCE = Registry.registerForHolder(BuiltInRegistries.POTION, Wildscapes.id("long_resistance"), new Potion("resistance", new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2400)));

    public static final Holder.Reference<Potion> STRONG_RESISTANCE = Registry.registerForHolder(BuiltInRegistries.POTION, Wildscapes.id("strong_resistance"), new Potion("resistance", new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1)));

    public static final Holder.Reference<Potion> GNASHING = Registry.registerForHolder(BuiltInRegistries.POTION, Wildscapes.id("gnashing"), new Potion("gnashing", new MobEffectInstance(WildscapesEffects.GNASHING, 900)));

    public static final Holder.Reference<Potion> LONG_GNASHING = Registry.registerForHolder(BuiltInRegistries.POTION, Wildscapes.id("long_gnashing"), new Potion("gnashing", new MobEffectInstance(WildscapesEffects.GNASHING, 1800)));

    public static final Holder.Reference<Potion> STRONG_GNASHING = Registry.registerForHolder(BuiltInRegistries.POTION, Wildscapes.id("strong_gnashing"), new Potion("gnashing", new MobEffectInstance(WildscapesEffects.GNASHING, 640, 1)));

    public static final Holder.Reference<Potion> INCENDIARY = Registry.registerForHolder(BuiltInRegistries.POTION, Wildscapes.id("incendiary"), new Potion("incendiary", new MobEffectInstance(WildscapesEffects.INCENDIARY, 1)));

    public static void register() {}
}
