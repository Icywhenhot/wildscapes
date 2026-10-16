package com.wildscapes.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import com.wildscapes.block.CauldronOfSoulsBlock;
import com.wildscapes.block.IncendiaryFireBlock;
import com.wildscapes.item.WildscapesItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class IncendiaryBottle extends ThrowableItemProjectile {
    @Nullable
    private UUID target;
    @Nullable
    private BlockPos cauldron;
    private int flightTime;

    public IncendiaryBottle(EntityType<? extends IncendiaryBottle> type, Level level) {
        super(type, level);
    }

    public IncendiaryBottle(Level level, LivingEntity thrower) {
        super(WildscapesEntities.INCENDIARY_BOTTLE.get(), thrower, level);
    }

    public IncendiaryBottle(Level level, double x, double y, double z) {
        super(WildscapesEntities.INCENDIARY_BOTTLE.get(), x, y, z, level);
    }

    public void haunt(BlockPos pos, Player player) {
        cauldron = pos.immutable();
        target = player.getUUID();
        setNoGravity(true);
    }

    public boolean cameFrom(BlockPos pos) {
        return pos.equals(cauldron);
    }

    @Override
    public void tick() {
        if (target != null && level() instanceof ServerLevel level) {
            Entity entity = level.getEntity(target);
            var state = level.getBlockState(cauldron);
            if (++flightTime > 240 || !(entity instanceof Player player) || !player.isAlive()
                    || player.isCreative() || player.isSpectator() || distanceToSqr(player) > 4096
                    || !(state.getBlock() instanceof CauldronOfSoulsBlock)
                    || state.getValue(CauldronOfSoulsBlock.PHASE) != CauldronOfSoulsBlock.Phase.ACTIVE) {
                discard();
                return;
            }
            if (flightTime <= 30) {
                setDeltaMovement(Math.sin(flightTime * 0.2) * 0.015, 0.045, Math.cos(flightTime * 0.2) * 0.015);
            } else {
                Vec3 aim = player.position().add(0, player.getBbHeight() * 0.5, 0).subtract(position()).normalize().scale(0.34);
                setDeltaMovement(getDeltaMovement().scale(0.85).add(aim.scale(0.15)));
            }
            if (flightTime % 2 == 0) {
                level.sendParticles(ParticleTypes.SOUL, getX(), getY(), getZ(), 1, 0.05, 0.05, 0.05, 0.0);
            }
        }
        super.tick();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return (target == null || flightTime > 30 && target.equals(entity.getUUID())) && super.canHitEntity(entity);
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
        Vec3 at = result.getLocation();
        if (result instanceof EntityHitResult hit && hit.getEntity() instanceof Player player) {
            at = player.position();
            player.hurt(damageSources().thrown(this, getOwner()), 3.0F);
            player.igniteForSeconds(5.0F);
        }
        IncendiaryFireBlock.spread(level(), at);
        discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (target != null) {
            tag.putUUID("Target", target);
            tag.putLong("Cauldron", cauldron.asLong());
            tag.putInt("FlightTime", flightTime);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Target")) {
            target = tag.getUUID("Target");
            cauldron = BlockPos.of(tag.getLong("Cauldron"));
            flightTime = tag.getInt("FlightTime");
            setNoGravity(true);
        }
    }
}
