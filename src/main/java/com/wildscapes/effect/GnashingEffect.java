package com.wildscapes.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;

public class GnashingEffect extends MobEffect {
    private static final int WARNING = 20;

    public GnashingEffect() {
        super(MobEffectCategory.HARMFUL, 0x9C3C77);
    }

    private static int every(int amplifier) {
        return amplifier >= 2 ? 20 : amplifier == 1 ? 40 : 60;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration > WARNING && duration % every(amplifier) == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return true;
        }
        double x = entity.getBlockX() + 0.5;
        double y = entity.getBlockY();
        double z = entity.getBlockZ() + 0.5;
        level.addFreshEntity(new EvokerFangs(level, x, y, z, entity.getYRot() * ((float) Math.PI / 180F), WARNING, null));
        level.sendParticles(ParticleTypes.ENCHANT, x, y + 0.1, z, 24, 0.3, 0.05, 0.3, 0.6);
        level.sendParticles(ParticleTypes.WITCH, x, y + 0.1, z, 6, 0.3, 0.05, 0.3, 0.0);
        return true;
    }
}
