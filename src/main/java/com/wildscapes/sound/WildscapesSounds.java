package com.wildscapes.sound;

import com.wildscapes.Wildscapes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Sound events used by the Abomination's GeckoLib animations. The animation
 * {@code sound_effects} keyframes reference these by their full id
 * (e.g. {@code wildscapes:abomination.croak}), and GeckoLib's
 * {@code AutoPlayingSoundKeyframeHandler} plays them when the keyframe is hit.
 *
 * <p>The {@code .ogg} files live in {@code assets/wildscapes/sounds/abomination/}
 * (converted from the author-supplied {@code .wav}/{@code .mp3} clips). The
 * animation keyframe sounds ({@code step}, {@code croak}, {@code tongue},
 * {@code slurp}, and the five {@code jump_*} stages) are played by GeckoLib; the
 * {@code ambient}, {@code hurt} and {@code death} events are played from
 * {@link com.wildscapes.entity.AbominationEntity}.
 */
public final class WildscapesSounds {
    private WildscapesSounds() {}

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, Wildscapes.MODID);

    // Animation keyframe sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_STEP        = register("abomination.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_CROAK       = register("abomination.croak");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_TONGUE      = register("abomination.tongue");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_SLURP       = register("abomination.slurp");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_JUMP_CHARGE = register("abomination.jump_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_JUMP_WINDUP = register("abomination.jump_windup");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_JUMP_LEAP   = register("abomination.jump_leap");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_JUMP_FALL   = register("abomination.jump_fall");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_JUMP_LAND   = register("abomination.jump_land");

    // Swim / water-entry sounds (play from the swim animation's keyframes)
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_WATER_STAND  = register("abomination.water_stand");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_WATER_SPLASH = register("abomination.water_splash");

    // Gameplay sounds (played from entity code, not animation keyframes)
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_AMBIENT = register("abomination.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_HURT    = register("abomination.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABOMINATION_DEATH   = register("abomination.death");

    // Boiling-cauldron / magic-soup sounds (played from CauldronBlockEntity and the cauldron block)
    public static final DeferredHolder<SoundEvent, SoundEvent> CAULDRON_BOIL_START     = register("cauldron.boil_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAULDRON_ADD_INGREDIENT = register("cauldron.add_ingredient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAULDRON_MIX            = register("cauldron.mix");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAULDRON_SOUP_DONE      = register("cauldron.soup_done");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, name)));
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
