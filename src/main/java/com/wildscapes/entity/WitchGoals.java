package com.wildscapes.entity;

import java.util.List;

import com.wildscapes.block.WildscapesBlocks;
import com.wildscapes.effect.WildscapesPotions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class WitchGoals {
    private static final String CHECKED = "wildscapes:cauldron_checked";
    private static final String HOME = "wildscapes:cauldron_home";
    private static final ResourceLocation DRINKING_SPEED = ResourceLocation.withDefaultNamespace("drinking");

    private WitchGoals() {}

    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof Witch witch) {
            addGoals(witch);
            CompoundTag data = witch.getPersistentData();
            if (data.contains(HOME)) {
                witch.restrictTo(BlockPos.of(data.getLong(HOME)), 12);
            } else if (event.loadedFromDisk()) {
                data.putBoolean(CHECKED, true);
            }
        } else if (event.getEntity() instanceof ThrownPotion potion
                && potion.getOwner() instanceof Witch witch
                && witch.getTarget() instanceof Player player
                && player.getHealth() <= player.getMaxHealth() * 0.4F) {
            potion.setItem(PotionContents.createItemStack(Items.SPLASH_POTION,
                    witch.getRandom().nextBoolean() ? Potions.WEAKNESS : Potions.SLOWNESS));
        }
    }

    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Witch witch) || witch.level().isClientSide()) {
            return;
        }

        if (witch.isDrinkingPotion() && witch.getTarget() != null) {
            witch.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(DRINKING_SPEED);
        }

        CompoundTag data = witch.getPersistentData();
        if (!data.getBoolean(CHECKED)) {
            findCauldron(witch);
            data.putBoolean(CHECKED, true);
        }
        if (data.contains(HOME) && witch.tickCount % 100 == 0) {
            BlockPos home = BlockPos.of(data.getLong(HOME));
            if (!isCauldron(witch, home)) {
                witch.clearRestriction();
                data.remove(HOME);
            }
        }

        if (witch.isDrinkingPotion() || witch.hasEffect(MobEffects.DAMAGE_RESISTANCE)
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
        if (witch.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrapped -> wrapped.getGoal() instanceof WitchCombatGoal)) {
            return;
        }

        List.copyOf(witch.goalSelector.getAvailableGoals()).stream()
                .filter(wrapped -> wrapped.getGoal() instanceof RangedAttackGoal)
                .forEach(wrapped -> witch.goalSelector.removeGoal(wrapped.getGoal()));
        witch.goalSelector.addGoal(1, new MoveTowardsRestrictionGoal(witch, 1.0D));
        witch.goalSelector.addGoal(2, new WitchCombatGoal(witch));
        witch.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(witch, Raider.class, 10, true, false,
                target -> target != witch && target.getType() != EntityType.WITCH
                        && target.getHealth() < target.getMaxHealth()));
    }

    private static void findCauldron(Witch witch) {
        BlockPos.findClosestMatch(witch.blockPosition(), 8, 4, pos -> isCauldron(witch, pos))
                .ifPresent(pos -> {
                    witch.restrictTo(pos, 12);
                    witch.getPersistentData().putLong(HOME, pos.asLong());
                });
    }

    private static boolean isCauldron(Witch witch, BlockPos pos) {
        if (!witch.level().hasChunk(SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getZ()))) {
            return false;
        }
        var state = witch.level().getBlockState(pos);
        return state.getBlock() instanceof AbstractCauldronBlock || state.is(WildscapesBlocks.CAULDRON_OF_SOULS.get());
    }
}
