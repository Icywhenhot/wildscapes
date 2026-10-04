package com.wildscapes.effect;

import javax.annotation.Nullable;

import com.wildscapes.block.IncendiaryFireBlock;

import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class IncendiaryEffect extends InstantenousMobEffect {
    public IncendiaryEffect() {
        super(MobEffectCategory.HARMFUL, 0xB23FD6);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            IncendiaryFireBlock.spread(entity.level(), entity.position());
        }
        return true;
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirectSource, LivingEntity entity,
            int amplifier, double health) {
        applyEffectTick(entity, amplifier);
    }
}
