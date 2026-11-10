package com.wildscapes.entity;

import java.util.List;

import com.wildscapes.sound.WildscapesSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.IForgeShearable;

public class Bogged extends AbstractSkeleton implements Shearable, IForgeShearable {
    private static final EntityDataAccessor<Boolean> SHEARED = SynchedEntityData.defineId(Bogged.class, EntityDataSerializers.BOOLEAN);

    public Bogged(EntityType<? extends Bogged> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSkeleton.createAttributes().add(Attributes.MAX_HEALTH, 16.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SHEARED, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("sheared", isSheared());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(SHEARED, tag.getBoolean("sheared"));
    }

    public boolean isSheared() {
        return entityData.get(SHEARED);
    }

    @Override
    public void reassessWeaponGoal() {
        super.reassessWeaponGoal();
        if (level() != null && !level().isClientSide) {
            bowGoal.setMinAttackInterval(level().getDifficulty() == Difficulty.HARD ? 50 : 70);
        }
    }

    @Override
    protected AbstractArrow getArrow(ItemStack stack, float damage) {
        AbstractArrow arrow = super.getArrow(stack, damage);
        if (arrow instanceof Arrow tipped) {
            tipped.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
        }
        return arrow;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.SHEARS) && readyForShearing()) {
            if (!level().isClientSide) {
                shear(SoundSource.PLAYERS);
                gameEvent(GameEvent.SHEAR, player);
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean readyForShearing() {
        return isAlive() && !isSheared();
    }

    @Override
    public boolean isShearable(ItemStack item, Level level, BlockPos pos) {
        return readyForShearing();
    }

    @Override
    public List<ItemStack> onSheared(Player player, ItemStack item, Level level, BlockPos pos, int fortune) {
        return mushrooms(player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS);
    }

    @Override
    public void shear(SoundSource source) {
        for (ItemStack stack : mushrooms(source)) {
            spawnAtLocation(stack, getBbHeight());
        }
    }

    private List<ItemStack> mushrooms(SoundSource source) {
        level().playSound(null, this, WildscapesSounds.BOGGED_SHEAR.get(), source, 1.0F, 1.0F);
        entityData.set(SHEARED, true);
        return List.of(new ItemStack(random.nextBoolean() ? Items.BROWN_MUSHROOM : Items.RED_MUSHROOM),
                new ItemStack(random.nextBoolean() ? Items.BROWN_MUSHROOM : Items.RED_MUSHROOM));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return WildscapesSounds.BOGGED_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return WildscapesSounds.BOGGED_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WildscapesSounds.BOGGED_DEATH.get();
    }

    @Override
    protected SoundEvent getStepSound() {
        return WildscapesSounds.BOGGED_STEP.get();
    }
}
