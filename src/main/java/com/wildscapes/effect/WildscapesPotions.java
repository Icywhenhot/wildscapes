package com.wildscapes.effect;

import com.wildscapes.Wildscapes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class WildscapesPotions {
    private WildscapesPotions() {}

    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(Registries.POTION, Wildscapes.MODID);

    public static final RegistryObject<Potion> MIRAGE = POTIONS.register("mirage",
            () -> new Potion(new MobEffectInstance(WildscapesEffects.MIRAGE.get(), 3600)));

    public static final RegistryObject<Potion> LONG_MIRAGE = POTIONS.register("long_mirage",
            () -> new Potion("mirage", new MobEffectInstance(WildscapesEffects.MIRAGE.get(), 9600)));

    public static final RegistryObject<Potion> RESISTANCE = POTIONS.register("resistance",
            () -> new Potion(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200)));

    public static final RegistryObject<Potion> LONG_RESISTANCE = POTIONS.register("long_resistance",
            () -> new Potion("resistance", new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2400)));

    public static final RegistryObject<Potion> STRONG_RESISTANCE = POTIONS.register("strong_resistance",
            () -> new Potion("resistance", new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1)));

    public static final RegistryObject<Potion> GNASHING = POTIONS.register("gnashing",
            () -> new Potion(new MobEffectInstance(WildscapesEffects.GNASHING.get(), 900)));

    public static final RegistryObject<Potion> LONG_GNASHING = POTIONS.register("long_gnashing",
            () -> new Potion("gnashing", new MobEffectInstance(WildscapesEffects.GNASHING.get(), 1800)));

    public static final RegistryObject<Potion> STRONG_GNASHING = POTIONS.register("strong_gnashing",
            () -> new Potion("gnashing", new MobEffectInstance(WildscapesEffects.GNASHING.get(), 640, 1)));

    public static final RegistryObject<Potion> INCENDIARY = POTIONS.register("incendiary",
            () -> new Potion(new MobEffectInstance(WildscapesEffects.INCENDIARY.get(), 1)));

    public static void register(IEventBus bus) {
        POTIONS.register(bus);
    }
}
