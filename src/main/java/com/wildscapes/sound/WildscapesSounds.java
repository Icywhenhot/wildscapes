package com.wildscapes.sound;

import com.wildscapes.Wildscapes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public final class WildscapesSounds {
    private WildscapesSounds() {}

    public static final SoundEvent ABOMINATION_STEP = register("abomination.step");
    public static final SoundEvent ABOMINATION_CROAK = register("abomination.croak");
    public static final SoundEvent ABOMINATION_TONGUE = register("abomination.tongue");
    public static final SoundEvent ABOMINATION_SLURP = register("abomination.slurp");
    public static final SoundEvent ABOMINATION_JUMP_CHARGE = register("abomination.jump_charge");
    public static final SoundEvent ABOMINATION_JUMP_WINDUP = register("abomination.jump_windup");
    public static final SoundEvent ABOMINATION_JUMP_LEAP = register("abomination.jump_leap");
    public static final SoundEvent ABOMINATION_JUMP_FALL = register("abomination.jump_fall");
    public static final SoundEvent ABOMINATION_JUMP_LAND = register("abomination.jump_land");

    public static final SoundEvent ABOMINATION_WATER_STAND = register("abomination.water_stand");
    public static final SoundEvent ABOMINATION_WATER_SPLASH = register("abomination.water_splash");

    public static final SoundEvent ABOMINATION_AMBIENT = register("abomination.ambient");
    public static final SoundEvent ABOMINATION_HURT = register("abomination.hurt");
    public static final SoundEvent ABOMINATION_DEATH = register("abomination.death");

    public static final SoundEvent CAULDRON_BOIL_START = register("cauldron.boil_start");
    public static final SoundEvent CAULDRON_ADD_INGREDIENT = register("cauldron.add_ingredient");
    public static final SoundEvent CAULDRON_MIX = register("cauldron.mix");
    public static final SoundEvent CAULDRON_SOUP_DONE = register("cauldron.soup_done");

    public static final SoundEvent MIRELASH_THROW = register("mirelash.throw");
    public static final SoundEvent MIRELASH_HIT = register("mirelash.hit");
    public static final SoundEvent MIRELASH_IMPACT = register("mirelash.impact");
    public static final SoundEvent NECKLACE_ACTIVATION = register("necklace.activation");

    public static final SoundEvent CAULDRON_OF_SOULS_HUM = register("cauldron_of_souls.hum");
    public static final SoundEvent CAULDRON_OF_SOULS_ACTIVATE = register("cauldron_of_souls.activate");
    public static final SoundEvent CAULDRON_OF_SOULS_REWARD = register("cauldron_of_souls.reward");
    public static final SoundEvent EMPOWERED_DEATH = register("soul_harvest.empowered_death");
    public static final SoundEvent SUMMONING_BONFIRE_IGNITE = register("summoning_bonfire.ignite");

    public static final SoundEvent EMPOWERED_JUMP_CHARGE = register("empowered_jump.charge");
    public static final SoundEvent EMPOWERED_JUMP_LAUNCH = register("empowered_jump.launch");
    public static final SoundEvent EMPOWERED_JUMP_READY = register("empowered_jump.ready");
    public static final SoundEvent MULTI_JUMP = register("multi_jump");

    public static final SoundEvent SHELF_ACTIVATE = register("shelf.activate");
    public static final SoundEvent SHELF_DEACTIVATE = register("shelf.deactivate");
    public static final SoundEvent SHELF_TAKE_ITEM = register("shelf.take_item");
    public static final SoundEvent SHELF_SINGLE_SWAP = register("shelf.single_swap");
    public static final SoundEvent SHELF_PLACE_ITEM = register("shelf.place_item");
    public static final SoundEvent SHELF_MULTI_SWAP = register("shelf.multi_swap");

    private static SoundEvent register(String name) {
        ResourceLocation id = Wildscapes.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void register() {}
}
