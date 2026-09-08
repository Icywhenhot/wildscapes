package com.wildscapes.entity;

import java.util.EnumSet;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class WitchCombatGoal extends Goal {
    private static final float ATTACK_RANGE = 10.0F;
    private final Witch witch;
    private LivingEntity target;
    private int attackTime = -1;
    private int seeTime;
    private int strafeTime = -1;
    private int strafePathTime;
    private boolean clockwise;
    private boolean backwards;

    public WitchCombatGoal(Witch witch) {
        this.witch = witch;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.witch.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        this.target = target;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() || this.target != null && this.target.isAlive()
                && !this.witch.getNavigation().isDone();
    }

    @Override
    public void stop() {
        this.target = null;
        this.seeTime = 0;
        this.attackTime = -1;
        this.strafeTime = -1;
        this.strafePathTime = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.target == null) {
            return;
        }

        double dist = this.witch.distanceToSqr(this.target);
        boolean visible = this.witch.getSensing().hasLineOfSight(this.target);
        if (visible) {
            this.seeTime++;
        } else {
            this.seeTime = 0;
        }

        if (this.target instanceof Player && dist <= ATTACK_RANGE * ATTACK_RANGE && visible) {
            this.strafeTime++;
            this.strafe(dist);
        } else if (dist > ATTACK_RANGE * ATTACK_RANGE || this.seeTime < 5) {
            this.witch.getNavigation().moveTo(this.target, 1.0D);
            this.strafeTime = -1;
            this.strafePathTime = 0;
        } else {
            this.witch.getNavigation().stop();
            this.strafePathTime = 0;
        }

        this.witch.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        if (--this.attackTime == 0) {
            if (!visible) {
                return;
            }
            this.witch.performRangedAttack(this.target,
                    Mth.clamp((float)Math.sqrt(dist) / ATTACK_RANGE, 0.1F, 1.0F));
            this.attackTime = 60;
        } else if (this.attackTime < 0) {
            this.attackTime = 60;
        }
    }

    private void strafe(double dist) {
        if (this.strafeTime >= 20) {
            if (this.witch.getRandom().nextFloat() < 0.3F) {
                this.clockwise = !this.clockwise;
                this.strafePathTime = 0;
            }
            if (this.witch.getRandom().nextFloat() < 0.3F) {
                this.backwards = !this.backwards;
                this.strafePathTime = 0;
            }
            this.strafeTime = 0;
        }

        if (dist > 64.0D && this.backwards) {
            this.backwards = false;
            this.strafePathTime = 0;
        } else if (dist < 36.0D && !this.backwards) {
            this.backwards = true;
            this.strafePathTime = 0;
        }

        if (--this.strafePathTime <= 0 || this.witch.getNavigation().isDone()) {
            Vec3 away = this.witch.position().subtract(this.target.position()).multiply(1.0, 0.0, 1.0);
            if (away.lengthSqr() > 0.001) {
                away = away.normalize();
                Vec3 side = this.clockwise ? new Vec3(-away.z, 0.0, away.x)
                        : new Vec3(away.z, 0.0, -away.x);
                double retreat = this.backwards ? 1.0 : -0.2;
                Vec3 direction = side.scale(0.75).add(away.scale(retreat)).normalize();
                Vec3 wanted = this.witch.position().add(direction.scale(8.0));
                Vec3 next = LandRandomPos.getPosTowards(this.witch, 6, 3, wanted);
                if (next != null && this.witch.getNavigation().moveTo(next.x, next.y, next.z,
                        this.backwards ? 1.0D : 0.85D)) {
                    this.strafePathTime = 8 + this.witch.getRandom().nextInt(5);
                    return;
                }
            }
            this.strafePathTime = 4;
        }

        if (this.witch.getNavigation().isDone()) {
            this.witch.getMoveControl().strafe(this.backwards ? -1.0F : 0.65F,
                    this.clockwise ? 0.75F : -0.75F);
        }
    }
}
