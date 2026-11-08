package com.wildscapes.effect;

import org.jspecify.annotations.Nullable;

import com.wildscapes.block.IncendiaryFireBlock;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.InstantaneousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class IncendiaryEffect extends InstantaneousMobEffect {
    public IncendiaryEffect() {
        super(MobEffectCategory.HARMFUL, 0xB23FD6);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        IncendiaryFireBlock.spread(level, entity.position());
        return true;
    }

    @Override
    public void applyInstantaneousEffect(ServerLevel level, @Nullable Entity source, @Nullable Entity indirectSource, LivingEntity entity,
            int amplifier, double health) {
        applyEffectTick(level, entity, amplifier);
    }
}
