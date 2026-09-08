package com.wildscapes.entity;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import com.wildscapes.effect.WildscapesEffects;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.player.Player;

final class IllusionerMirageGoal extends Goal {
    private static final double RANGE = 10.0;
    private static final int WARMUP_TICKS = 20;
    private static final int EFFECT_DURATION = 300;
    private static final int COOLDOWN = 200;
    private static final String CLONED_MOB_TAG_PREFIX = "wildscapes:illusioner_cloned/";

    private final Illusioner illusioner;
    private Mob ally;
    private int warmup;
    private int nextUseTick;

    IllusionerMirageGoal(Illusioner illusioner) {
        this.illusioner = illusioner;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (illusioner.tickCount < nextUseTick || illusioner.isCastingSpell()) {
            return false;
        }
        LivingEntity target = illusioner.getTarget();
        if (!(target instanceof Player) || !target.isAlive()) {
            return false;
        }
        ally = findAttacker(target);
        return ally != null;
    }

    @Override
    public boolean canContinueToUse() {
        return warmup > 0 && ally != null && ally.isAlive()
                && illusioner.getTarget() instanceof Player target && target.isAlive()
                && ally.getTarget() == target && ally.distanceToSqr(illusioner) <= RANGE * RANGE;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        warmup = WARMUP_TICKS;
        illusioner.getNavigation().stop();
        IllusionerSpellBridge.startCasting(illusioner, WARMUP_TICKS + 1);
        illusioner.playSound(SoundEvents.ILLUSIONER_PREPARE_MIRROR, 1.0F, 1.0F);
    }

    @Override
    public void stop() {
        IllusionerSpellBridge.stopCasting(illusioner);
        nextUseTick = illusioner.tickCount + COOLDOWN;
        ally = null;
    }

    @Override
    public void tick() {
        illusioner.getLookControl().setLookAt(ally, 30.0F, 30.0F);
        if (--warmup == 0) {
            if (ally.addEffect(new MobEffectInstance(WildscapesEffects.MIRAGE, EFFECT_DURATION), illusioner)) {
                rememberClone(ally);
            }
            illusioner.level().playSound(null, illusioner.getX(), illusioner.getY(), illusioner.getZ(),
                    SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }

    private Mob findAttacker(LivingEntity target) {
        List<Mob> nearby = illusioner.level().getEntitiesOfClass(Mob.class,
                illusioner.getBoundingBox().inflate(RANGE),
                mob -> mob != illusioner
                        && mob.isAlive()
                        && mob.getTarget() == target
                        && mob.distanceToSqr(illusioner) <= RANGE * RANGE
                        && illusioner.hasLineOfSight(mob)
                        && !mob.hasEffect(WildscapesEffects.MIRAGE)
                        && !wasCloned(mob));
        return nearby.stream().min((a, b) -> Double.compare(illusioner.distanceToSqr(a), illusioner.distanceToSqr(b))).orElse(null);
    }

    private boolean wasCloned(LivingEntity entity) {
        return illusioner.getPersistentData().getBoolean(clonedMobKey(entity.getUUID()));
    }

    private void rememberClone(LivingEntity entity) {
        illusioner.getPersistentData().putBoolean(clonedMobKey(entity.getUUID()), true);
    }

    private static String clonedMobKey(UUID uuid) {
        return CLONED_MOB_TAG_PREFIX + uuid;
    }
}
