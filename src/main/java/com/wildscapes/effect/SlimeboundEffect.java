package com.wildscapes.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public final class SlimeboundEffect extends MobEffect {
    public SlimeboundEffect() {
        super(MobEffectCategory.HARMFUL, 0x62C654);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        Vec3 movement = entity.getDeltaMovement();
        entity.setDeltaMovement(0.0, Math.min(movement.y, 0.0), 0.0);
        entity.hasImpulse = true;
        entity.hurtMarked = true;
        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
