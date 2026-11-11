package com.wildscapes.sound;

import com.wildscapes.Wildscapes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class WildscapesSounds {
    private WildscapesSounds() {}

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, Wildscapes.MODID);

    public static final RegistryObject<SoundEvent> ABOMINATION_STEP        = register("abomination.step");
    public static final RegistryObject<SoundEvent> ABOMINATION_CROAK       = register("abomination.croak");
    public static final RegistryObject<SoundEvent> ABOMINATION_TONGUE      = register("abomination.tongue");
    public static final RegistryObject<SoundEvent> ABOMINATION_SLURP       = register("abomination.slurp");
    public static final RegistryObject<SoundEvent> ABOMINATION_JUMP_CHARGE = register("abomination.jump_charge");
    public static final RegistryObject<SoundEvent> ABOMINATION_JUMP_WINDUP = register("abomination.jump_windup");
    public static final RegistryObject<SoundEvent> ABOMINATION_JUMP_LEAP   = register("abomination.jump_leap");
    public static final RegistryObject<SoundEvent> ABOMINATION_JUMP_FALL   = register("abomination.jump_fall");
    public static final RegistryObject<SoundEvent> ABOMINATION_JUMP_LAND   = register("abomination.jump_land");

    public static final RegistryObject<SoundEvent> ABOMINATION_WATER_STAND  = register("abomination.water_stand");
    public static final RegistryObject<SoundEvent> ABOMINATION_WATER_SPLASH = register("abomination.water_splash");

    public static final RegistryObject<SoundEvent> ABOMINATION_AMBIENT = register("abomination.ambient");
    public static final RegistryObject<SoundEvent> ABOMINATION_HURT    = register("abomination.hurt");
    public static final RegistryObject<SoundEvent> ABOMINATION_DEATH   = register("abomination.death");

    public static final RegistryObject<SoundEvent> CAULDRON_BOIL_START     = register("cauldron.boil_start");
    public static final RegistryObject<SoundEvent> CAULDRON_ADD_INGREDIENT = register("cauldron.add_ingredient");
    public static final RegistryObject<SoundEvent> CAULDRON_MIX            = register("cauldron.mix");
    public static final RegistryObject<SoundEvent> CAULDRON_SOUP_DONE      = register("cauldron.soup_done");

    public static final RegistryObject<SoundEvent> MIRELASH_THROW          = register("mirelash.throw");
    public static final RegistryObject<SoundEvent> MIRELASH_HIT            = register("mirelash.hit");
    public static final RegistryObject<SoundEvent> MIRELASH_IMPACT         = register("mirelash.impact");
    public static final RegistryObject<SoundEvent> NECKLACE_ACTIVATION     = register("necklace.activation");

    public static final RegistryObject<SoundEvent> CAULDRON_OF_SOULS_HUM      = register("cauldron_of_souls.hum");
    public static final RegistryObject<SoundEvent> CAULDRON_OF_SOULS_ACTIVATE = register("cauldron_of_souls.activate");
    public static final RegistryObject<SoundEvent> CAULDRON_OF_SOULS_REWARD   = register("cauldron_of_souls.reward");
    public static final RegistryObject<SoundEvent> EMPOWERED_DEATH            = register("soul_harvest.empowered_death");
    public static final RegistryObject<SoundEvent> SUMMONING_BONFIRE_IGNITE   = register("summoning_bonfire.ignite");

    public static final RegistryObject<SoundEvent> EMPOWERED_JUMP_CHARGE = register("empowered_jump.charge");
    public static final RegistryObject<SoundEvent> EMPOWERED_JUMP_LAUNCH = register("empowered_jump.launch");
    public static final RegistryObject<SoundEvent> EMPOWERED_JUMP_READY = register("empowered_jump.ready");
    public static final RegistryObject<SoundEvent> MULTI_JUMP = register("multi_jump");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, name)));
    }

    public static final RegistryObject<SoundEvent> BOGGED_AMBIENT = register("entity.bogged.ambient");
    public static final RegistryObject<SoundEvent> BOGGED_HURT = register("entity.bogged.hurt");
    public static final RegistryObject<SoundEvent> BOGGED_DEATH = register("entity.bogged.death");
    public static final RegistryObject<SoundEvent> BOGGED_STEP = register("entity.bogged.step");
    public static final RegistryObject<SoundEvent> BOGGED_SHEAR = register("entity.bogged.shear");
    public static final RegistryObject<SoundEvent> OMINOUS_BOTTLE_DISPOSE = register("item.ominous_bottle.dispose");
    public static final RegistryObject<SoundEvent> TRIAL_SPAWNER_BREAK = register("block.trial_spawner.break");
    public static final RegistryObject<SoundEvent> TRIAL_SPAWNER_STEP = register("block.trial_spawner.step");
    public static final RegistryObject<SoundEvent> TRIAL_SPAWNER_PLACE = register("block.trial_spawner.place");
    public static final RegistryObject<SoundEvent> TRIAL_SPAWNER_HIT = register("block.trial_spawner.hit");
    public static final RegistryObject<SoundEvent> TRIAL_SPAWNER_FALL = register("block.trial_spawner.fall");
    public static final RegistryObject<SoundEvent> TRIAL_SPAWNER_SPAWN_MOB = register("block.trial_spawner.spawn_mob");

    public static final net.minecraft.world.level.block.SoundType SUMMONING_BONFIRE =
            new net.minecraftforge.common.util.ForgeSoundType(1.0F, 1.0F, TRIAL_SPAWNER_BREAK,
                    TRIAL_SPAWNER_STEP, TRIAL_SPAWNER_PLACE, TRIAL_SPAWNER_HIT, TRIAL_SPAWNER_FALL);

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
