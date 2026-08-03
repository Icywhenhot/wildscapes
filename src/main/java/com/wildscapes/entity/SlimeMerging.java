package com.wildscapes.entity;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.wildscapes.Wildscapes;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Small slimes that have been around for a while pair up and merge into a medium one.
 *
 * <p>Half a minute after spawning a size-1 slime starts looking for another one that is
 * also ready. Seeking reuses vanilla's own {@code SlimeAttackGoal} by simply setting the
 * other slime as its attack target — that goal already steers a slime towards whatever it
 * is targeting, and a size-1 slime deals no damage ({@code isDealsDamage} is false when
 * tiny), so they bump into each other harmlessly. It also means a nearby player still wins
 * the slime's attention, since vanilla's targeting goals outrank this.
 *
 * <p>Once they touch, both shrink away over a second while a fresh medium slime grows in
 * their place. The animation is driven by a transient modifier on the vanilla
 * {@link Attributes#SCALE} attribute rather than a custom packet: attribute modifiers are
 * synced to clients already, so the renderer can read the merge straight off the entity and
 * fade it out. See {@code RedesignedSlimeRenderer}.
 *
 * <p>A named slime loses its name — the merged slime is a brand new entity and nothing is
 * copied onto it.
 */
public final class SlimeMerging {
    private SlimeMerging() {}

    /** Marks a slime mid-merge, and carries how far along it is. */
    public static final ResourceLocation MERGE_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "merging");

    /** Takes scale from 1.0 down to 0.0625, the smallest the attribute allows. */
    public static final double MAX_SHRINK = 0.9375;

    private static final int READY_AFTER_TICKS = 600; // 30 seconds
    private static final int MERGE_TICKS = 20;        // 1 second of fading
    private static final double SEEK_RADIUS = 16.0;
    private static final double TOUCH_DISTANCE = 1.0;

    private static final String TAG_DELAY = "WildscapesMergeDelay";
    private static final String TAG_PARTNER = "WildscapesMergePartner";
    private static final String TAG_PROGRESS = "WildscapesMergeProgress";
    private static final String TAG_GROWING = "WildscapesMergeGrowing";

    public static void onEntityTick(EntityTickEvent.Post event) {
        // Magma cubes extend Slime but are left out of this deliberately.
        if (!(event.getEntity() instanceof Slime slime) || slime instanceof MagmaCube) {
            return;
        }
        if (slime.level().isClientSide || !slime.isAlive()) {
            return;
        }

        CompoundTag data = slime.getPersistentData();
        if (data.contains(TAG_GROWING)) {
            tickGrowing(slime, data);
        } else if (data.hasUUID(TAG_PARTNER)) {
            tickMerging(slime, data);
        } else if (slime.getSize() == 1) {
            seekPartner(slime, data);
        }
    }

    // ------------------------------------------------------------------ seeking

    private static void seekPartner(Slime slime, CompoundTag data) {
        if (!data.contains(TAG_DELAY)) {
            data.putInt(TAG_DELAY, READY_AFTER_TICKS);
            return;
        }
        int delay = data.getInt(TAG_DELAY);
        if (delay > 0) {
            data.putInt(TAG_DELAY, delay - 1);
            return;
        }
        // Chasing a player is more interesting than merging, so leave that alone. A slime
        // that was spawned with its AI switched off is left alone too, since the merge
        // turns AI back on when it finishes.
        if (slime.isNoAi() || slime.isPassenger() || slime.isVehicle() || slime.getTarget() instanceof Player) {
            return;
        }

        Slime partner = findPartner(slime);
        if (partner == null) {
            return;
        }
        slime.setTarget(partner);
        if (slime.distanceTo(partner) <= TOUCH_DISTANCE) {
            link(slime, partner);
            link(partner, slime);
        }
    }

    private static Slime findPartner(Slime slime) {
        List<Slime> candidates = slime.level().getEntitiesOfClass(Slime.class,
                slime.getBoundingBox().inflate(SEEK_RADIUS),
                other -> other != slime
                        && other.isAlive()
                        && other.getSize() == 1
                        && !(other instanceof MagmaCube)
                        && isReady(other)
                        && !isMerging(other));
        return candidates.stream().min(Comparator.comparingDouble(slime::distanceToSqr)).orElse(null);
    }

    private static boolean isReady(Slime slime) {
        CompoundTag data = slime.getPersistentData();
        return data.contains(TAG_DELAY) && data.getInt(TAG_DELAY) <= 0;
    }

    private static boolean isMerging(Slime slime) {
        CompoundTag data = slime.getPersistentData();
        return data.hasUUID(TAG_PARTNER) || data.contains(TAG_GROWING);
    }

    // ------------------------------------------------------------------ merging

    private static void link(Slime slime, Slime partner) {
        CompoundTag data = slime.getPersistentData();
        data.putUUID(TAG_PARTNER, partner.getUUID());
        data.putInt(TAG_PROGRESS, 0);
        slime.setTarget(null);
        slime.setNoAi(true);
    }

    private static void tickMerging(Slime slime, CompoundTag data) {
        Slime partner = partnerOf(slime, data);
        if (partner == null || !partner.isAlive()) {
            abort(slime, data);
            return;
        }

        int progress = data.getInt(TAG_PROGRESS) + 1;
        data.putInt(TAG_PROGRESS, progress);
        setShrink(slime, (double) progress / MERGE_TICKS);

        // Slide the pair together so they visibly meet before they vanish.
        Vec3 middle = slime.position().add(partner.position()).scale(0.5);
        Vec3 next = slime.position().lerp(middle, 0.35);
        slime.moveTo(next.x, next.y, next.z, slime.getYRot(), slime.getXRot());

        // One of the two runs the finish, so the result is not spawned twice.
        if (progress >= MERGE_TICKS && slime.getUUID().compareTo(partner.getUUID()) < 0) {
            spawnMerged(slime, middle);
            partner.discard();
            slime.discard();
        }
    }

    private static Slime partnerOf(Slime slime, CompoundTag data) {
        if (!(slime.level() instanceof ServerLevel level)) {
            return null;
        }
        UUID id = data.getUUID(TAG_PARTNER);
        Entity partner = level.getEntity(id);
        return partner instanceof Slime other && other.getSize() == 1 ? other : null;
    }

    /** Puts a slime back the way it was when its partner did not make it. */
    private static void abort(Slime slime, CompoundTag data) {
        data.remove(TAG_PARTNER);
        data.remove(TAG_PROGRESS);
        data.putInt(TAG_DELAY, READY_AFTER_TICKS);
        clearShrink(slime);
        slime.setNoAi(false);
    }

    private static void spawnMerged(Slime a, Vec3 where) {
        if (!(a.level() instanceof ServerLevel level)) {
            return;
        }
        Slime merged = EntityType.SLIME.create(level);
        if (merged == null) {
            return;
        }
        merged.setSize(2, true);
        merged.moveTo(where.x, where.y, where.z, a.getYRot(), 0.0F);
        // Nothing is carried over from the pair, so a named slime loses its name here.
        beginGrowing(merged);
        level.addFreshEntity(merged);

        level.sendParticles(ParticleTypes.ITEM_SLIME, where.x, where.y + 0.3, where.z, 24, 0.3, 0.3, 0.3, 0.0);
        level.playSound(null, where.x, where.y, where.z, SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.0F, 0.8F);
    }

    // ------------------------------------------------------------------ growing in

    private static void beginGrowing(Slime slime) {
        slime.getPersistentData().putInt(TAG_GROWING, MERGE_TICKS);
        setShrink(slime, 1.0);
        slime.setNoAi(true);
    }

    private static void tickGrowing(Slime slime, CompoundTag data) {
        int remaining = data.getInt(TAG_GROWING) - 1;
        if (remaining <= 0) {
            data.remove(TAG_GROWING);
            clearShrink(slime);
            slime.setNoAi(false);
            data.putInt(TAG_DELAY, READY_AFTER_TICKS);
            return;
        }
        data.putInt(TAG_GROWING, remaining);
        setShrink(slime, (double) remaining / MERGE_TICKS);
    }

    // ------------------------------------------------------------------ scale + alpha

    private static void setShrink(LivingEntity entity, double progress) {
        AttributeInstance scale = entity.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.addOrUpdateTransientModifier(new AttributeModifier(MERGE_MODIFIER_ID,
                    -MAX_SHRINK * Mth.clamp(progress, 0.0, 1.0),
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static void clearShrink(LivingEntity entity) {
        AttributeInstance scale = entity.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.removeModifier(MERGE_MODIFIER_ID);
        }
    }

    /**
     * How opaque a slime should be drawn, from the merge modifier alone. Reading the
     * modifier rather than the resulting scale keeps this from fading slimes that are
     * small for some unrelated reason, such as a command or another mod.
     *
     * <p>One expression covers both halves of the animation: the pair shrink towards zero
     * as their modifier grows, and the merged slime grows back as its modifier unwinds.
     */
    public static float mergeAlpha(LivingEntity entity) {
        AttributeInstance scale = entity.getAttribute(Attributes.SCALE);
        if (scale == null) {
            return 1.0F;
        }
        AttributeModifier modifier = scale.getModifier(MERGE_MODIFIER_ID);
        return modifier == null ? 1.0F : (float) Mth.clamp(1.0 + modifier.amount() / MAX_SHRINK, 0.0, 1.0);
    }
}
