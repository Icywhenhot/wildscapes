package com.wildscapes;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;

public final class WildscapesConfig {
    private WildscapesConfig() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<Value<?>> ALL = new ArrayList<>();

    public static final Value<Integer> NECKLACE_REVEAL_SECONDS = range("illusionist_necklace", "revealSeconds", 30, 0, 3600);
    public static final Value<Boolean> NECKLACE_HIDES_FROM_BOSSES = flag("illusionist_necklace", "hidesFromBosses", false);
    public static final Value<Boolean> NECKLACE_BLOCKS_DAMAGE = flag("illusionist_necklace", "blocksDamage", true);
    public static final Value<Boolean> NECKLACE_WALL_ESCAPE = flag("illusionist_necklace", "wallEscape", true);
    public static final Value<Integer> NECKLACE_DRAIN_TICKS = range("illusionist_necklace", "drainTicks", 20, 1, 1200);
    public static final Value<Double> NECKLACE_DROP_CHANCE = range("illusionist_necklace", "illusionerDropChance", 1.0, 0.0, 1.0);

    public static final Value<Integer> HARVEST_TRIGGER_RANGE = range("soul_harvest", "triggerRange", 16, 1, 64);
    public static final Value<Integer> HARVEST_MAX_ALIVE = range("soul_harvest", "maxAlive", 15, 1, 100);
    public static final Value<Double> EMPOWERED_HEALTH = range("soul_harvest", "empoweredHealthMultiplier", 2.0, 1.0, 20.0);
    public static final Value<Double> EMPOWERED_DAMAGE = range("soul_harvest", "empoweredDamageMultiplier", 2.0, 1.0, 20.0);

    public static final Value<Double> POUNCE_DAMAGE = range("abomination", "pounceDamage", 12.0, 0.0, 1000.0);
    public static final Value<Double> TONGUE_DAMAGE = range("abomination", "tongueDamage", 2.0, 0.0, 1000.0);

    public static final Value<Integer> DROWNED_RARITY = range("swamp", "drownedRarity", 20, 0, 10000);
    public static final Value<Integer> DAY_SLIME_RARITY = range("swamp", "daylightSlimeRarity", 100, 0, 10000);
    public static final Value<Boolean> SLIME_MERGING = flag("swamp", "slimeMerging", true);
    public static final Value<Integer> SLIME_MERGE_DELAY = range("swamp", "slimeMergeDelay", 30, 1, 3600);
    public static final Value<Boolean> WITCH_RITUALS = flag("swamp", "witchRituals", true);
    public static final Value<Boolean> FROG_LEGS = flag("swamp", "frogLegDrops", true);

    public static final Value<Boolean> SWAMP_FOG = flag("visuals", "swampFog", true);
    public static final Value<Double> SWAMP_FOG_STRENGTH = range("visuals", "swampFogStrength", 1.0, 0.0, 1.0);
    public static final Value<MobModels> MOB_MODELS = new Value<>("visuals", "mobModels", MobModels.AUTO) {
        @Override
        MobModels read(JsonElement json) {
            return MobModels.valueOf(json.getAsString().toUpperCase(Locale.ROOT));
        }

        @Override
        JsonElement write(MobModels value) {
            return new JsonPrimitive(value.name());
        }
    };

    public enum MobModels {
        AUTO,
        REDESIGNED,
        VANILLA
    }

    public static <T> T get(Value<T> value) {
        return value.get();
    }

    public static void load() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve(Wildscapes.MODID + ".json");
        JsonObject root = new JsonObject();
        if (Files.exists(file)) {
            try (Reader in = Files.newBufferedReader(file)) {
                JsonElement parsed = JsonParser.parseReader(in);
                if (parsed.isJsonObject()) {
                    root = parsed.getAsJsonObject();
                }
            } catch (IOException | JsonParseException e) {
                Wildscapes.LOGGER.warn("couldn't read {}, using defaults", file, e);
            }
        }
        for (Value<?> v : ALL) {
            v.load(root);
        }
        JsonObject out = new JsonObject();
        for (Value<?> v : ALL) {
            v.save(out);
        }
        try {
            Files.createDirectories(file.getParent());
            try (Writer w = Files.newBufferedWriter(file)) {
                GSON.toJson(out, w);
            }
        } catch (IOException e) {
            Wildscapes.LOGGER.warn("couldn't write {}", file, e);
        }
    }

    private static Value<Boolean> flag(String section, String key, boolean def) {
        return new Value<>(section, key, def) {
            @Override
            Boolean read(JsonElement json) {
                return json.getAsBoolean();
            }

            @Override
            JsonElement write(Boolean value) {
                return new JsonPrimitive(value);
            }
        };
    }

    private static Value<Integer> range(String section, String key, int def, int min, int max) {
        return new Value<>(section, key, def) {
            @Override
            Integer read(JsonElement json) {
                return Mth.clamp(json.getAsInt(), min, max);
            }

            @Override
            JsonElement write(Integer value) {
                return new JsonPrimitive(value);
            }
        };
    }

    private static Value<Double> range(String section, String key, double def, double min, double max) {
        return new Value<>(section, key, def) {
            @Override
            Double read(JsonElement json) {
                return Mth.clamp(json.getAsDouble(), min, max);
            }

            @Override
            JsonElement write(Double value) {
                return new JsonPrimitive(value);
            }
        };
    }

    public abstract static class Value<T> {
        private final String section;
        private final String key;
        private final T def;
        private T value;

        Value(String section, String key, T def) {
            this.section = section;
            this.key = key;
            this.def = def;
            this.value = def;
            ALL.add(this);
        }

        public T get() {
            return value;
        }

        abstract T read(JsonElement json);

        abstract JsonElement write(T value);

        void load(JsonObject root) {
            value = def;
            if (!root.has(section) || !root.get(section).isJsonObject()) {
                return;
            }
            JsonElement raw = root.getAsJsonObject(section).get(key);
            if (raw == null) {
                return;
            }
            try {
                value = read(raw);
            } catch (RuntimeException e) {
                Wildscapes.LOGGER.warn("bad config value {}.{}: {}", section, key, raw);
            }
        }

        void save(JsonObject root) {
            if (!root.has(section)) {
                root.add(section, new JsonObject());
            }
            root.getAsJsonObject(section).add(key, write(value));
        }
    }
}
