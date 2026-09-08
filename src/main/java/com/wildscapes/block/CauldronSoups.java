package com.wildscapes.block;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class CauldronSoups {
    private CauldronSoups() {}

    public static final Item BASE_INGREDIENT = Items.NETHER_WART;

    public static final int MAX_EFFECTS = 3;

    public static final int EFFECT_DURATION = 300;

    public static final int SHORT_DURATION = 160;

    private static final Map<Item, Holder<MobEffect>> EFFECTS = Map.ofEntries(
            Map.entry(Items.SUGAR, MobEffects.MOVEMENT_SPEED),
            Map.entry(Items.RABBIT_FOOT, MobEffects.JUMP),
            Map.entry(Items.BLAZE_POWDER, MobEffects.DAMAGE_BOOST),
            Map.entry(Items.GLISTERING_MELON_SLICE, MobEffects.HEAL),
            Map.entry(Items.SPIDER_EYE, MobEffects.POISON),
            Map.entry(Items.GHAST_TEAR, MobEffects.REGENERATION),
            Map.entry(Items.MAGMA_CREAM, MobEffects.FIRE_RESISTANCE),
            Map.entry(Items.PUFFERFISH, MobEffects.WATER_BREATHING),
            Map.entry(Items.GOLDEN_CARROT, MobEffects.NIGHT_VISION),
            Map.entry(Items.PHANTOM_MEMBRANE, MobEffects.SLOW_FALLING),
            Map.entry(Items.FERMENTED_SPIDER_EYE, MobEffects.WEAKNESS),
            Map.entry(Items.SLIME_BALL, MobEffects.MOVEMENT_SLOWDOWN),
            Map.entry(Items.GLOWSTONE_DUST, MobEffects.GLOWING));

    private static final Set<Holder<MobEffect>> STACKABLE = Set.of(
            MobEffects.MOVEMENT_SPEED,
            MobEffects.JUMP,
            MobEffects.DAMAGE_BOOST,
            MobEffects.HEAL,
            MobEffects.POISON,
            MobEffects.REGENERATION,
            MobEffects.MOVEMENT_SLOWDOWN);

    public static boolean isEffectIngredient(Item item) {
        return EFFECTS.containsKey(item);
    }

    @Nullable
    public static Holder<MobEffect> effectOf(Item item) {
        return EFFECTS.get(item);
    }

    public static int maxAmplifier(Holder<MobEffect> effect) {
        return STACKABLE.contains(effect) ? 1 : 0;
    }

    @Nullable
    public static MobEffectInstance effectFor(Item item) {
        Holder<MobEffect> effect = EFFECTS.get(item);
        return effect == null ? null : instanceOf(effect, 0);
    }

    public static MobEffectInstance instanceOf(Holder<MobEffect> effect, int amplifier) {
        boolean harmful = effect == MobEffects.POISON || effect == MobEffects.WEAKNESS
                || effect == MobEffects.MOVEMENT_SLOWDOWN;
        int duration = effect.value().isInstantenous() ? 1 : (harmful ? SHORT_DURATION : EFFECT_DURATION);
        return new MobEffectInstance(effect, duration, Math.min(amplifier, maxAmplifier(effect)));
    }

    public static int colorOf(List<MobEffectInstance> effects) {
        if (effects.isEmpty()) {
            return 0x8B5A2B;
        }
        int r = 0;
        int g = 0;
        int b = 0;
        int total = 0;
        for (MobEffectInstance effect : effects) {
            int color = effect.getEffect().value().getColor();
            int amplifier = effect.getAmplifier() + 1;
            r += ((color >> 16) & 0xFF) * amplifier;
            g += ((color >> 8) & 0xFF) * amplifier;
            b += (color & 0xFF) * amplifier;
            total += amplifier;
        }
        if (total == 0) {
            return 0x8B5A2B;
        }
        return ((r / total) << 16) | ((g / total) << 8) | (b / total);
    }
}
