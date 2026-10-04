package com.wildscapes.entity;

import com.wildscapes.block.IncendiaryFireBlock;
import com.wildscapes.item.WildscapesItems;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.HitResult;

public class IncendiaryBottle extends ThrowableItemProjectile {
    public IncendiaryBottle(EntityType<? extends IncendiaryBottle> type, Level level) {
        super(type, level);
    }

    public IncendiaryBottle(Level level, LivingEntity thrower) {
        super(WildscapesEntities.INCENDIARY_BOTTLE.get(), thrower, level);
    }

    public IncendiaryBottle(Level level, double x, double y, double z) {
        super(WildscapesEntities.INCENDIARY_BOTTLE.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return WildscapesItems.INCENDIARY_BOTTLE.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide) {
            return;
        }
        level().levelEvent(LevelEvent.PARTICLES_SPELL_POTION_SPLASH, blockPosition(), 0xB23FD6);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 0.8F, 1.1F);
        IncendiaryFireBlock.spread(level(), result.getLocation());
        discard();
    }
}
