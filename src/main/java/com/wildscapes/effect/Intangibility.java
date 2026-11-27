package com.wildscapes.effect;

import com.wildscapes.entity.WildscapesEntityData;

import com.wildscapes.WildscapesConfig;
import com.wildscapes.WildscapesAdvancements;
import com.wildscapes.item.WildscapesItems;
import com.wildscapes.sound.WildscapesSounds;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.Tags;

public final class Intangibility {
    private static final String ACTIVE_KEY = "wildscapes:necklace_active";
    private static final String INVISIBLE_KEY = "wildscapes:necklace_invisible";
    private static final String REVEALED_KEY = "wildscapes:revealed_to";

    private Intangibility() {}

    public static boolean isActive(Player player) {
        return player.isShiftKeyDown() && (canUse(player.getMainHandItem()) || canUse(player.getOffhandItem()));
    }

    public static boolean hiddenFrom(Player player, Entity observer) {
        if (!isActive(player)) {
            return false;
        }
        if (observer == null || observer.level().isClientSide()) {
            return true;
        }
        if (!WildscapesConfig.get(WildscapesConfig.NECKLACE_HIDES_FROM_BOSSES)
                && observer.typeHolder().is(Tags.EntityTypes.BOSSES)) {
            return false;
        }
        CompoundTag seen = ((WildscapesEntityData) observer).wildscapes$data().getCompoundOrEmpty(REVEALED_KEY);
        return seen.getLongOr(player.getStringUUID(), Long.MIN_VALUE) < observer.level().getGameTime();
    }

    private static void reveal(Entity victim, Player player) {
        int secs = WildscapesConfig.get(WildscapesConfig.NECKLACE_REVEAL_SECONDS);
        if (secs <= 0) {
            return;
        }
        long now = victim.level().getGameTime();
        CompoundTag data = ((WildscapesEntityData) victim).wildscapes$data();
        CompoundTag seen = data.getCompoundOrEmpty(REVEALED_KEY);
        for (String key : seen.keySet().toArray(String[]::new)) {
            if (seen.getLongOr(key, 0L) < now) {
                seen.remove(key);
            }
        }
        seen.putLong(player.getStringUUID(), now + secs * 20L);
        data.put(REVEALED_KEY, seen);
    }

    private static boolean canUse(ItemStack stack) {
        return stack.is(WildscapesItems.ILLUSIONIST_NECKLACE) && stack.getDamageValue() < stack.getMaxDamage() - 1;
    }

    public static void onMobTick(Entity ticking) {
        if (!(ticking instanceof Mob mob) || mob.level().isClientSide()) {
            return;
        }
        if (mob.getTarget() instanceof Player player && hiddenFrom(player, mob)) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
        var brain = mob.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) instanceof Player player
                && hiddenFrom(player, mob)) {
            brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
            mob.getNavigation().stop();
        }
    }

    public static void onPlayerTick(Entity ticking) {
        if (!(ticking instanceof Player player)) {
            return;
        }

        boolean active = isActive(player);
        if (active && !player.level().isClientSide() && !player.getAbilities().instabuild) {
            var data = ((WildscapesEntityData) player).wildscapes$data();
            int ticks = data.getIntOr("wildscapes:necklace_ticks", 0) + 1;
            if (ticks >= WildscapesConfig.get(WildscapesConfig.NECKLACE_DRAIN_TICKS)) {
                EquipmentSlot slot = canUse(player.getMainHandItem()) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                player.getItemBySlot(slot).hurtAndBreak(1, player, slot);
                ticks = 0;
                active = isActive(player);
            }
            data.putInt("wildscapes:necklace_ticks", ticks);
        }
        if (player.level() instanceof ServerLevel level) {
            var data = ((WildscapesEntityData) player).wildscapes$data();
            boolean wasActive = data.getBooleanOr(ACTIVE_KEY, false);
            if (active) {
                if (!player.isInvisible()) {
                    player.setInvisible(true);
                    data.putBoolean(INVISIBLE_KEY, true);
                }
                if (!wasActive) {
                    WildscapesAdvancements.award(player, "now_you_see_me_now_you_dont");
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            WildscapesSounds.NECKLACE_ACTIVATION, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                if (player.tickCount % 5 == 0) {
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 0.6,
                            player.getZ(), 1, 0.2, 0.35, 0.2, 0.01);
                }
            } else if (data.getBooleanOr(INVISIBLE_KEY, false)) {
                if (!player.hasEffect(MobEffects.INVISIBILITY) && !player.isSpectator()) {
                    player.setInvisible(false);
                }
                data.remove(INVISIBLE_KEY);
            }
            if (wasActive && !active && player.isInWall()
                    && WildscapesConfig.get(WildscapesConfig.NECKLACE_WALL_ESCAPE)) {
                escapeWall(player, level);
            }
            data.putBoolean(ACTIVE_KEY, active);
        }
        MobEffectInstance effect = player.getEffect(WildscapesEffects.INTANGIBILITY);
        if (active) {
            if (effect == null) {
                player.addEffect(new MobEffectInstance(WildscapesEffects.INTANGIBILITY,
                        MobEffectInstance.INFINITE_DURATION, 0, false, false, true));
            }
        } else if (effect != null) {
            player.removeEffect(WildscapesEffects.INTANGIBILITY);
        }
    }

    private static void escapeWall(Player player, ServerLevel level) {
        BlockPos spot = findSafeSpot(player, level);
        if (spot == null) {
            return;
        }

        double x = spot.getX() + 0.5;
        double y = spot.getY();
        double z = spot.getZ() + 0.5;
        level.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 0.9, player.getZ(),
                30, 0.3, 0.5, 0.3, 0.02);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        player.teleportTo(x, y, z);
        player.resetFallDistance();
        level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.9, z, 30, 0.3, 0.5, 0.3, 0.02);
        level.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static BlockPos findSafeSpot(Player player, ServerLevel level) {
        BlockPos origin = player.blockPosition();
        BlockPos grounded = searchAround(player, level, origin, true);
        return grounded != null ? grounded : searchAround(player, level, origin, false);
    }

    private static BlockPos searchAround(Player player, ServerLevel level, BlockPos origin, boolean needsFloor) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        double nearest = Double.MAX_VALUE;
        for (int dx = -8; dx <= 8; dx++) {
            for (int dy = -5; dy <= 5; dy++) {
                for (int dz = -8; dz <= 8; dz++) {
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    double dist = cursor.distSqr(origin);
                    if (dist >= nearest || !level.isInWorldBounds(cursor)) {
                        continue;
                    }
                    if (fits(player, level, cursor, needsFloor)) {
                        nearest = dist;
                        best = cursor.immutable();
                    }
                }
            }
        }
        return best;
    }

    private static boolean fits(Player player, ServerLevel level, BlockPos pos, boolean needsFloor) {
        if (!level.getFluidState(pos).isEmpty()) {
            return false;
        }
        AABB box = player.getDimensions(player.getPose())
                .makeBoundingBox(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        if (!level.noCollision(player, box)) {
            return false;
        }
        if (!needsFloor) {
            return true;
        }
        BlockPos below = pos.below();
        return !level.getBlockState(below).getCollisionShape(level, below).isEmpty();
    }

    public static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker instanceof Player player && attacker != entity) {
            reveal(entity, player);
        }
        return !(entity instanceof Player player && attacker != null && attacker != player
                && WildscapesConfig.get(WildscapesConfig.NECKLACE_BLOCKS_DAMAGE)
                && hiddenFrom(player, attacker));
    }
}
