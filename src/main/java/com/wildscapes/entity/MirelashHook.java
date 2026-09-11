package com.wildscapes.entity;

import java.util.UUID;

import com.wildscapes.effect.WildscapesEffects;
import com.wildscapes.item.MirelashItem;
import com.wildscapes.sound.WildscapesSounds;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class MirelashHook extends ThrowableProjectile {
    private static final EntityDataAccessor<Boolean> ANCHORED = SynchedEntityData.defineId(MirelashHook.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> TETHER_LENGTH = SynchedEntityData.defineId(MirelashHook.class,
            EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_RANGE = SynchedEntityData.defineId(MirelashHook.class,
            EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> SLIME_SNARE = SynchedEntityData.defineId(MirelashHook.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ATTACK = SynchedEntityData.defineId(MirelashHook.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(MirelashHook.class,
            EntityDataSerializers.INT);

    public MirelashHook(EntityType<? extends MirelashHook> type, Level level) {
        super(type, level);
    }

    public MirelashHook(Level level, Player owner, ItemStack stack) {
        super(WildscapesEntities.MIRELASH_HOOK.get(), owner, level);
        int elasticity = MirelashItem.enchantmentLevel(level, stack, MirelashItem.ELASTICITY);
        entityData.set(MAX_RANGE, 30.0F * (elasticity + 1));
        entityData.set(SLIME_SNARE, MirelashItem.enchantmentLevel(level, stack, MirelashItem.SLIME_SNARE) > 0);
        setDeltaMovement(owner.getLookAngle().scale(5.0));
    }

    public MirelashHook(Level level, Player owner, ItemStack stack, LivingEntity target) {
        super(WildscapesEntities.MIRELASH_HOOK.get(), owner, level);
        entityData.set(MAX_RANGE, (float) MirelashItem.ATTACK_RANGE);
        entityData.set(SLIME_SNARE, MirelashItem.enchantmentLevel(level, stack, MirelashItem.SLIME_SNARE) > 0);
        entityData.set(ATTACK, true);
        entityData.set(TARGET, target.getId());
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ANCHORED, false);
        builder.define(TETHER_LENGTH, 1.5F);
        builder.define(MAX_RANGE, 30.0F);
        builder.define(SLIME_SNARE, false);
        builder.define(ATTACK, false);
        builder.define(TARGET, -1);
    }

    @Override
    public void tick() {
        Entity owner = getOwner();
        if (!(owner instanceof Player player) || !player.isAlive() || !isHeldBy(player)
                || distanceToSqr(player) > entityData.get(MAX_RANGE) * entityData.get(MAX_RANGE)) {
            discard();
            return;
        }
        if (entityData.get(ATTACK)) {
            tickAttack(player);
            return;
        }
        if (isAnchored()) {
            setNoGravity(true);
            setDeltaMovement(0.0, 0.0, 0.0);
        }
        super.tick();
        if (isAnchored()) {
            setDeltaMovement(0.0, 0.0, 0.0);
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entityData.get(ATTACK) && entity.getId() == entityData.get(TARGET) && super.canHitEntity(entity);
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (!level().isClientSide) {
            level().playSound(null, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z,
                    WildscapesSounds.MIRELASH_IMPACT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        if (entityData.get(ATTACK)) {
            discard();
            return;
        }
        setPos(hit.getLocation());
        setDeltaMovement(0.0, 0.0, 0.0);
        setNoGravity(true);
        entityData.set(ANCHORED, true);
        Entity owner = getOwner();
        if (owner != null) {
            float length = Math.max((float) owner.getEyePosition().distanceTo(hit.getLocation()) * 0.5F - 3.0F, 1.5F);
            entityData.set(TETHER_LENGTH, length);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!level().isClientSide && entityData.get(ATTACK) && hit.getEntity() instanceof LivingEntity target
                && getOwner() instanceof Player player) {
            landAttack(player, target);
        }
    }

    private void tickAttack(Player player) {
        Entity entity = level().getEntity(entityData.get(TARGET));
        if (!(entity instanceof LivingEntity target) || !target.isAlive()) {
            discard();
            return;
        }
        Vec3 targetPos = new Vec3(target.getX(), target.getY(0.5), target.getZ());
        Vec3 travel = targetPos.subtract(position());
        if (!level().isClientSide && (tickCount >= 3 || travel.lengthSqr() < 0.36)) {
            landAttack(player, target);
            return;
        }
        setNoGravity(true);
        setDeltaMovement(travel.scale(0.5));
        super.tick();
    }

    private void landAttack(Player player, LivingEntity target) {
        float distance = Mth.clamp(player.distanceTo(target), 0.0F, (float) MirelashItem.ATTACK_RANGE);
        float damage = Mth.lerp(distance / (float) MirelashItem.ATTACK_RANGE, 9.0F, 6.0F);
        target.hurt(level().damageSources().playerAttack(player), damage);
        level().playSound(null, target.getX(), target.getY(0.5), target.getZ(), WildscapesSounds.MIRELASH_HIT.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
        if (entityData.get(SLIME_SNARE)) {
            target.addEffect(new MobEffectInstance(WildscapesEffects.SLIMEBOUND, 80, 0, false, true, false));
            ServerLevel level = (ServerLevel) level();
            ItemParticleOption slime = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SLIME_BALL));
            level.sendParticles(slime, target.getX(), target.getY(0.55), target.getZ(), 28,
                    target.getBbWidth() * 0.45, target.getBbHeight() * 0.35, target.getBbWidth() * 0.45, 0.08);
        }
        discard();
    }

    @Override
    public void remove(RemovalReason reason) {
        Entity owner = getOwner();
        UUID hookId = getUUID();
        super.remove(reason);
        if (owner instanceof Player player) {
            MirelashItem.clearHook(player, hookId);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("anchored", isAnchored());
        tag.putFloat("tether_length", getTetherLength());
        tag.putFloat("max_range", entityData.get(MAX_RANGE));
        tag.putBoolean("slime_snare", entityData.get(SLIME_SNARE));
        tag.putBoolean("attack", entityData.get(ATTACK));
        tag.putInt("target", entityData.get(TARGET));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(ANCHORED, tag.getBoolean("anchored"));
        entityData.set(TETHER_LENGTH, tag.getFloat("tether_length"));
        entityData.set(MAX_RANGE, tag.getFloat("max_range"));
        entityData.set(SLIME_SNARE, tag.getBoolean("slime_snare"));
        entityData.set(ATTACK, tag.getBoolean("attack"));
        entityData.set(TARGET, tag.getInt("target"));
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double range = entityData.get(MAX_RANGE) + 16.0;
        return distance < range * range;
    }

    public boolean isAnchored() {
        return entityData.get(ANCHORED);
    }

    public float getTetherLength() {
        return entityData.get(TETHER_LENGTH);
    }

    private boolean isHeldBy(Player player) {
        return player.getMainHandItem().getItem() instanceof MirelashItem
                || player.getOffhandItem().getItem() instanceof MirelashItem;
    }
}
