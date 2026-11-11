package com.wildscapes;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public final class WildscapesConfig {
    private WildscapesConfig() {}

    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final IntValue NECKLACE_REVEAL_SECONDS;
    public static final BooleanValue NECKLACE_HIDES_FROM_BOSSES;
    public static final BooleanValue NECKLACE_BLOCKS_DAMAGE;
    public static final BooleanValue NECKLACE_WALL_ESCAPE;
    public static final IntValue NECKLACE_DRAIN_TICKS;
    public static final DoubleValue NECKLACE_DROP_CHANCE;

    public static final IntValue HARVEST_TRIGGER_RANGE;
    public static final IntValue HARVEST_MAX_ALIVE;
    public static final DoubleValue EMPOWERED_HEALTH;
    public static final DoubleValue EMPOWERED_DAMAGE;

    public static final DoubleValue POUNCE_DAMAGE;
    public static final DoubleValue TONGUE_DAMAGE;

    public static final IntValue DROWNED_RARITY;
    public static final IntValue DAY_SLIME_RARITY;
    public static final BooleanValue SLIME_MERGING;
    public static final IntValue SLIME_MERGE_DELAY;
    public static final BooleanValue WITCH_RITUALS;
    public static final BooleanValue FROG_LEGS;

    public static final BooleanValue SWAMP_FOG;
    public static final DoubleValue SWAMP_FOG_STRENGTH;
    public static final EnumValue<MobModels> MOB_MODELS;

    public enum MobModels {
        AUTO,
        REDESIGNED,
        VANILLA
    }

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("illusionist_necklace");
        NECKLACE_REVEAL_SECONDS = b
                .comment("Seconds a mob can still see and target you after you hurt it while holding the necklace. 0 keeps the old behaviour where striking a mob never reveals you")
                .defineInRange("revealSeconds", 30, 0, 3600);
        NECKLACE_HIDES_FROM_BOSSES = b
                .comment("Whether the necklace also hides you from mobs in the forge:bosses tag")
                .define("hidesFromBosses", false);
        NECKLACE_BLOCKS_DAMAGE = b
                .comment("Whether mobs you are hidden from can't damage you at all")
                .define("blocksDamage", true);
        NECKLACE_WALL_ESCAPE = b
                .comment("Teleport the player out of solid blocks when the necklace turns off inside a wall")
                .define("wallEscape", true);
        NECKLACE_DRAIN_TICKS = b
                .comment("Ticks of use per point of durability lost")
                .defineInRange("drainTicks", 20, 1, 1200);
        NECKLACE_DROP_CHANCE = b
                .comment("Chance an illusioner drops the necklace on death")
                .defineInRange("illusionerDropChance", 1.0, 0.0, 1.0);
        b.pop();

        b.push("soul_harvest");
        HARVEST_TRIGGER_RANGE = b
                .comment("How close a player with Bad Omen has to be to an idle Cauldron of Souls to start a harvest")
                .defineInRange("triggerRange", 16, 1, 64);
        HARVEST_MAX_ALIVE = b
                .comment("Most raid mobs alive at once before the bonfires stop summoning")
                .defineInRange("maxAlive", 15, 1, 100);
        EMPOWERED_HEALTH = b
                .comment("Health multiplier for empowered mobs")
                .defineInRange("empoweredHealthMultiplier", 2.0, 1.0, 20.0);
        EMPOWERED_DAMAGE = b
                .comment("Damage multiplier for empowered mobs")
                .defineInRange("empoweredDamageMultiplier", 2.0, 1.0, 20.0);
        b.pop();

        b.push("abomination");
        POUNCE_DAMAGE = b.defineInRange("pounceDamage", 12.0, 0.0, 1000.0);
        TONGUE_DAMAGE = b.defineInRange("tongueDamage", 2.0, 0.0, 1000.0);
        b.pop();

        b.push("swamp");
        DROWNED_RARITY = b
                .comment("1 in N chance for an extra drowned spawn attempt in swamp water. 0 turns it off")
                .defineInRange("drownedRarity", 20, 0, 10000);
        DAY_SLIME_RARITY = b
                .comment("1 in N chance for a small slime to spawn in swamps during the day. 0 turns it off")
                .defineInRange("daylightSlimeRarity", 100, 0, 10000);
        SLIME_MERGING = b
                .comment("Small slimes find each other and merge into a bigger one")
                .define("slimeMerging", true);
        SLIME_MERGE_DELAY = b
                .comment("Seconds a small slime waits before looking for a partner")
                .defineInRange("slimeMergeDelay", 30, 1, 3600);
        WITCH_RITUALS = b
                .comment("Witches occasionally gather around a cauldron to chant")
                .define("witchRituals", true);
        FROG_LEGS = b
                .comment("Frogs drop frog legs")
                .define("frogLegDrops", true);
        b.pop();

        SERVER_SPEC = b.build();

        ForgeConfigSpec.Builder c = new ForgeConfigSpec.Builder();
        c.push("visuals");
        SWAMP_FOG = c.define("swampFog", true);
        SWAMP_FOG_STRENGTH = c.defineInRange("swampFogStrength", 1.0, 0.0, 1.0);
        MOB_MODELS = c
                .comment("Which models witches, illagers and slimes use. AUTO switches a mob back to its vanilla model and texture when a resource pack changes it, like Fresh Animations. Needs a resource reload (F3+T)")
                .defineEnum("mobModels", MobModels.AUTO);
        c.pop();
        CLIENT_SPEC = c.build();
    }

    public static <T> T get(ConfigValue<T> value) {
        return SERVER_SPEC.isLoaded() ? value.get() : value.getDefault();
    }
}
