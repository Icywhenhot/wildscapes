package com.wildscapes.entity;

import net.minecraft.world.entity.EntityTypes;
import java.util.List;

import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.effect.WildscapesPotions;

import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public final class WitchGoals {
    private static final String CHECKED = "wildscapes:cauldron_checked";
    private static final String HOME = "wildscapes:cauldron_home";
    private static final Identifier DRINKING_SPEED = Identifier.withDefaultNamespace("drinking");

    private WitchGoals() {}

    public static void onJoinLevel(Entity entity, ServerLevel level) {
        if (entity instanceof Witch witch) {
            addGoals(witch);
            CompoundTag data = ((WildscapesEntityData) witch).wildscapes$data();
            if (data.contains(HOME)) {
                witch.setHomeTo(BlockPos.of(data.getLongOr(HOME, 0)), 12);
            }
        } else if (entity instanceof ThrownSplashPotion potion
                && potion.getOwner() instanceof Witch witch
                && witch.getTarget() instanceof Player player
                && player.getHealth() <= player.getMaxHealth() * 0.4F) {
            potion.setItem(PotionContents.createItemStack(Items.SPLASH_POTION,
                    witch.getRandom().nextBoolean() ? Potions.WEAKNESS : Potions.SLOWNESS));
        }
    }

    public static void onEntityTick(Entity ticking) {
        if (!(ticking instanceof Witch witch) || witch.level().isClientSide()) {
            return;
        }

        WitchRitual.maybeStart(witch);

        if (witch.isDrinkingPotion() && witch.getTarget() != null) {
            witch.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(DRINKING_SPEED);
        }

        CompoundTag data = ((WildscapesEntityData) witch).wildscapes$data();
        if (!data.getBooleanOr(CHECKED, false)) {
            findCauldron(witch);
            data.putBoolean(CHECKED, true);
        }
        if (data.contains(HOME) && witch.tickCount % 100 == 0) {
            BlockPos home = BlockPos.of(data.getLongOr(HOME, 0));
            if (!isCauldron(witch, home)) {
                witch.clearHome();
                data.remove(HOME);
            }
        }

        if (witch.isDrinkingPotion() || witch.hasEffect(MobEffects.RESISTANCE)
                || !(witch.getTarget() instanceof Player player)
                || !witch.getSensing().hasLineOfSight(player)
                || witch.distanceToSqr(player) < 36.0D
                || (witch.tickCount + witch.getId()) % 100 != 0
                || witch.getRandom().nextFloat() >= 0.25F) {
            return;
        }

        witch.setItemSlot(EquipmentSlot.MAINHAND,
                PotionContents.createItemStack(Items.POTION, WildscapesPotions.RESISTANCE));
        witch.usingTime = witch.getMainHandItem().getUseDuration(witch);
        witch.setUsingItem(true);
        if (!witch.isSilent()) {
            witch.level().playSound(null, witch.getX(), witch.getY(), witch.getZ(), SoundEvents.WITCH_DRINK,
                    witch.getSoundSource(), 1.0F, 0.8F + witch.getRandom().nextFloat() * 0.4F);
        }
    }

    private static void addGoals(Witch witch) {
        if (witch.getGoalSelector().getAvailableGoals().stream()
                .anyMatch(wrapped -> wrapped.getGoal() instanceof WitchCombatGoal)) {
            return;
        }

        List.copyOf(witch.getGoalSelector().getAvailableGoals()).stream()
                .filter(wrapped -> wrapped.getGoal() instanceof RangedAttackGoal)
                .forEach(wrapped -> witch.getGoalSelector().removeGoal(wrapped.getGoal()));
        witch.getGoalSelector().addGoal(0, new WitchRitual.Gather(witch));
        witch.getGoalSelector().addGoal(1, new MoveTowardsRestrictionGoal(witch, 1.0D));
        witch.getGoalSelector().addGoal(2, new WitchCombatGoal(witch));
        witch.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(witch, Raider.class, 10, true, false,
                (target, server) -> target != witch && target.getType() != EntityTypes.WITCH
                        && target.getHealth() < target.getMaxHealth()));
    }

    private static void findCauldron(Witch witch) {
        BlockPos.findClosestMatch(witch.blockPosition(), 8, 4, pos -> isCauldron(witch, pos))
                .ifPresent(pos -> {
                    witch.setHomeTo(pos, 12);
                    ((WildscapesEntityData) witch).wildscapes$data().putLong(HOME, pos.asLong());
                });
    }

    private static boolean isCauldron(Witch witch, BlockPos pos) {
        if (!witch.level().hasChunk(SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getZ()))) {
            return false;
        }
        var state = witch.level().getBlockState(pos);
        return state.getBlock() instanceof AbstractCauldronBlock || state.is(WildscapesBlocks.CAULDRON_OF_SOULS);
    }
}
