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

/**
 * Maps brewing ingredients to the effect they add to a magic soup, and derives a soup's
 * tint colour from its effects. The mapping mirrors vanilla potion brewing (sugar → speed,
 * spider eye → poison, …) but every effect is short-lived, since a soup trades duration for
 * being drinkable almost instantly and carrying several effects at once.
 */
public final class CauldronSoups {
    private CauldronSoups() {}

    /** Nether wart starts a soup; it adds no effect of its own. */
    public static final Item BASE_INGREDIENT = Items.NETHER_WART;

    /** The most effects a single soup can hold. */
    public static final int MAX_EFFECTS = 3;

    /** Effect length of a freshly mixed ingredient, in ticks — ~15 s, well under a potion. */
    public static final int EFFECT_DURATION = 300;

    /** Poison/harm are punishing, so they run shorter still (~8 s). */
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

    /**
     * The effects vanilla brewing can push past level I (with glowstone). A soup never goes above
     * the strongest level the game already has for an effect, so there is no Night Vision II and
     * no Strength III.
     */
    private static final Set<Holder<MobEffect>> STACKABLE = Set.of(
            MobEffects.MOVEMENT_SPEED,
            MobEffects.JUMP,
            MobEffects.DAMAGE_BOOST,
            MobEffects.HEAL,
            MobEffects.POISON,
            MobEffects.REGENERATION,
            MobEffects.MOVEMENT_SLOWDOWN);

    /** Whether {@code item} is a recognised effect ingredient (not the nether wart base). */
    public static boolean isEffectIngredient(Item item) {
        return EFFECTS.containsKey(item);
    }

    /** The effect a given ingredient contributes, or null if it is not an ingredient. */
    @Nullable
    public static Holder<MobEffect> effectOf(Item item) {
        return EFFECTS.get(item);
    }

    /** The highest amplifier an effect may reach: 1 (level II) if vanilla has one, else 0. */
    public static int maxAmplifier(Holder<MobEffect> effect) {
        return STACKABLE.contains(effect) ? 1 : 0;
    }

    /** The effect instance a given ingredient contributes, or null if it is not an ingredient. */
    @Nullable
    public static MobEffectInstance effectFor(Item item) {
        Holder<MobEffect> effect = EFFECTS.get(item);
        return effect == null ? null : instanceOf(effect, 0);
    }

    /** A freshly brewed instance of {@code effect}, clamped to the level vanilla allows. */
    public static MobEffectInstance instanceOf(Holder<MobEffect> effect, int amplifier) {
        boolean harmful = effect == MobEffects.POISON || effect == MobEffects.WEAKNESS
                || effect == MobEffects.MOVEMENT_SLOWDOWN;
        int duration = effect.value().isInstantenous() ? 1 : (harmful ? SHORT_DURATION : EFFECT_DURATION);
        return new MobEffectInstance(effect, duration, Math.min(amplifier, maxAmplifier(effect)));
    }

    /**
     * A colour for the soup surface and the bowl, blended from the effect particle colours the
     * same way {@code PotionContents} blends a potion's colour. Falls back to a broth brown when
     * the soup has no effects yet.
     */
    public static int colorOf(List<MobEffectInstance> effects) {
        if (effects.isEmpty()) {
            return 0x8B5A2B; // broth brown
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
