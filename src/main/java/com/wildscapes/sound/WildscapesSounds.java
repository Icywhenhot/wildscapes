package com.wildscapes.sound;

import com.wildscapes.Wildscapes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WildscapesSounds {
    private WildscapesSounds() {}

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, Wildscapes.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_STEP        = register("abomination.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_CROAK       = register("abomination.croak");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_TONGUE      = register("abomination.tongue");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_SLURP       = register("abomination.slurp");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_JUMP_CHARGE = register("abomination.jump_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_JUMP_WINDUP = register("abomination.jump_windup");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_JUMP_LEAP   = register("abomination.jump_leap");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_JUMP_FALL   = register("abomination.jump_fall");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_JUMP_LAND   = register("abomination.jump_land");

    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_WATER_STAND  = register("abomination.water_stand");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_WATER_SPLASH = register("abomination.water_splash");

    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_AMBIENT = register("abomination.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_HURT    = register("abomination.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_DEATH   = register("abomination.death");

    public static final DeferredHolder<SoundEvent, SoundEvent> CAULDRON_BOIL_START     = register("cauldron.boil_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAULDRON_ADD_INGREDIENT = register("cauldron.add_ingredient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAULDRON_MIX            = register("cauldron.mix");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAULDRON_SOUP_DONE      = register("cauldron.soup_done");

    public static final DeferredHolder<SoundEvent, SoundEvent> MIRELASH_THROW          = register("mirelash.throw");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRELASH_HIT            = register("mirelash.hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRELASH_IMPACT         = register("mirelash.impact");
    public static final DeferredHolder<SoundEvent, SoundEvent> NECKLACE_ACTIVATION     = register("necklace.activation");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, name)));
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
