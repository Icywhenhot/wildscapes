package com.wildscapes;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public final class WildscapesConfig {
    private WildscapesConfig() {}

    public static final ModConfigSpec SERVER_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;

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
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.translation("config.wildscapes.illusionist_necklace").push("illusionist_necklace");
        NECKLACE_REVEAL_SECONDS = b
                .comment("Seconds a mob can still see and target you after you hurt it while holding the necklace. 0 keeps the old behaviour where striking a mob never reveals you")
                .translation("config.wildscapes.illusionist_necklace.revealSeconds")
                .defineInRange("revealSeconds", 30, 0, 3600);
        NECKLACE_HIDES_FROM_BOSSES = b
                .comment("Whether the necklace also hides you from mobs in the c:bosses tag")
                .translation("config.wildscapes.illusionist_necklace.hidesFromBosses")
                .define("hidesFromBosses", false);
        NECKLACE_BLOCKS_DAMAGE = b
                .comment("Whether mobs you are hidden from can't damage you at all")
                .translation("config.wildscapes.illusionist_necklace.blocksDamage")
                .define("blocksDamage", true);
        NECKLACE_WALL_ESCAPE = b
                .comment("Teleport the player out of solid blocks when the necklace turns off inside a wall")
                .translation("config.wildscapes.illusionist_necklace.wallEscape")
                .define("wallEscape", true);
        NECKLACE_DRAIN_TICKS = b
                .comment("Ticks of use per point of durability lost")
                .translation("config.wildscapes.illusionist_necklace.drainTicks")
                .defineInRange("drainTicks", 20, 1, 1200);
        NECKLACE_DROP_CHANCE = b
                .comment("Chance an illusioner drops the necklace on death")
                .translation("config.wildscapes.illusionist_necklace.illusionerDropChance")
                .defineInRange("illusionerDropChance", 1.0, 0.0, 1.0);
        b.pop();

        b.translation("config.wildscapes.soul_harvest").push("soul_harvest");
        HARVEST_TRIGGER_RANGE = b
                .comment("How close a player with Bad Omen has to be to an idle Cauldron of Souls to start a harvest")
                .translation("config.wildscapes.soul_harvest.triggerRange")
                .defineInRange("triggerRange", 16, 1, 64);
        HARVEST_MAX_ALIVE = b
                .comment("Most raid mobs alive at once before the bonfires stop summoning")
                .translation("config.wildscapes.soul_harvest.maxAlive")
                .defineInRange("maxAlive", 15, 1, 100);
        EMPOWERED_HEALTH = b
                .comment("Health multiplier for empowered mobs")
                .translation("config.wildscapes.soul_harvest.empoweredHealthMultiplier")
                .defineInRange("empoweredHealthMultiplier", 2.0, 1.0, 20.0);
        EMPOWERED_DAMAGE = b
                .comment("Damage multiplier for empowered mobs")
                .translation("config.wildscapes.soul_harvest.empoweredDamageMultiplier")
                .defineInRange("empoweredDamageMultiplier", 2.0, 1.0, 20.0);
        b.pop();

        b.translation("config.wildscapes.abomination").push("abomination");
        POUNCE_DAMAGE = b.translation("config.wildscapes.abomination.pounceDamage")
                .defineInRange("pounceDamage", 12.0, 0.0, 1000.0);
        TONGUE_DAMAGE = b.translation("config.wildscapes.abomination.tongueDamage")
                .defineInRange("tongueDamage", 2.0, 0.0, 1000.0);
        b.pop();

        b.translation("config.wildscapes.swamp").push("swamp");
        DROWNED_RARITY = b
                .comment("1 in N chance for an extra drowned spawn attempt in swamp water. 0 turns it off")
                .translation("config.wildscapes.swamp.drownedRarity")
                .defineInRange("drownedRarity", 20, 0, 10000);
        DAY_SLIME_RARITY = b
                .comment("1 in N chance for a small slime to spawn in swamps during the day. 0 turns it off")
                .translation("config.wildscapes.swamp.daylightSlimeRarity")
                .defineInRange("daylightSlimeRarity", 100, 0, 10000);
        SLIME_MERGING = b
                .comment("Small slimes find each other and merge into a bigger one")
                .translation("config.wildscapes.swamp.slimeMerging")
                .define("slimeMerging", true);
        SLIME_MERGE_DELAY = b
                .comment("Seconds a small slime waits before looking for a partner")
                .translation("config.wildscapes.swamp.slimeMergeDelay")
                .defineInRange("slimeMergeDelay", 30, 1, 3600);
        WITCH_RITUALS = b
                .comment("Witches occasionally gather around a cauldron to chant")
                .translation("config.wildscapes.swamp.witchRituals")
                .define("witchRituals", true);
        FROG_LEGS = b
                .comment("Frogs drop frog legs")
                .translation("config.wildscapes.swamp.frogLegDrops")
                .define("frogLegDrops", true);
        b.pop();

        SERVER_SPEC = b.build();

        ModConfigSpec.Builder c = new ModConfigSpec.Builder();
        c.translation("config.wildscapes.visuals").push("visuals");
        SWAMP_FOG = c.translation("config.wildscapes.visuals.swampFog")
                .define("swampFog", true);
        SWAMP_FOG_STRENGTH = c.translation("config.wildscapes.visuals.swampFogStrength")
                .defineInRange("swampFogStrength", 1.0, 0.0, 1.0);
        MOB_MODELS = c
                .comment("Which models witches, illagers and slimes use. AUTO switches a mob back to its vanilla model and texture when a resource pack changes it, like Fresh Animations, unless the pack also animates the Wildscapes models. Needs a resource reload (F3+T)")
                .translation("config.wildscapes.visuals.mobModels")
                .defineEnum("mobModels", MobModels.AUTO);
        c.pop();
        CLIENT_SPEC = c.build();
    }

    public static <T> T get(ConfigValue<T> value) {
        return SERVER_SPEC.isLoaded() ? value.get() : value.getDefault();
    }
}
