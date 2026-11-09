package com.wildscapes.effect;

import com.wildscapes.Wildscapes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class WildscapesEffects {
    private WildscapesEffects() {}

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Wildscapes.MODID);

    public static final RegistryObject<MobEffect> MIRAGE = EFFECTS.register("mirage",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x6E5FCD) {});

    public static final RegistryObject<MobEffect> SLIMEBOUND = EFFECTS.register("slimebound",
            SlimeboundEffect::new);

    public static final RegistryObject<MobEffect> INTANGIBILITY = EFFECTS.register("intangibility",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x7795C4) {});

    public static final RegistryObject<MobEffect> GNASHING = EFFECTS.register("gnashing", GnashingEffect::new);

    public static final RegistryObject<MobEffect> INCENDIARY = EFFECTS.register("incendiary",
            IncendiaryEffect::new);

    public static final RegistryObject<MobEffect> SOUL_HARVEST = EFFECTS.register("soul_harvest",
            () -> new MobEffect(MobEffectCategory.NEUTRAL, 0xC2336F) {});

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}
