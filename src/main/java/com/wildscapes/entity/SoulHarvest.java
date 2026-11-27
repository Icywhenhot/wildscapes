package com.wildscapes.entity;

import com.wildscapes.network.WildscapesNetwork;

import com.wildscapes.Wildscapes;
import com.wildscapes.WildscapesConfig;
import com.wildscapes.block.entity.CauldronOfSoulsBlockEntity;
import com.wildscapes.particle.WildscapesParticles;

import com.wildscapes.Backports;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class SoulHarvest {
    private SoulHarvest() {}

    public static final String EMPOWERED = "wildscapes:empowered";
    public static final String ORIGIN = "wildscapes:soul_harvest_origin";
    private static final java.util.UUID HEALTH_BOOST = java.util.UUID.nameUUIDFromBytes(
            "wildscapes:empowered_health".getBytes(java.nio.charset.StandardCharsets.UTF_8));

    public static boolean isEmpowered(Entity entity) {
        return WildscapesEntityData.of(entity).getBoolean(EMPOWERED);
    }

    public static void empower(Mob mob) {
        WildscapesEntityData.of(mob).putBoolean(EMPOWERED, true);
        WildscapesNetwork.sync(mob);
        AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(HEALTH_BOOST);
            health.addPermanentModifier(new AttributeModifier(HEALTH_BOOST, "empowered health",
                    WildscapesConfig.get(WildscapesConfig.EMPOWERED_HEALTH) - 1.0,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
            mob.setHealth(mob.getMaxHealth());
        }
    }

    public static float damage(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker instanceof Mob && isEmpowered(attacker)) {
            return amount * WildscapesConfig.get(WildscapesConfig.EMPOWERED_DAMAGE).floatValue();
        }
        return amount;
    }

    public static void onDeath(LivingEntity dead, DamageSource source) {
        if (!(dead.level() instanceof ServerLevel level)) {
            return;
        }
        if (!WildscapesEntityData.of(dead).contains(ORIGIN)) {
            CauldronOfSoulsBlockEntity running = CauldronOfSoulsBlockEntity.harvesting(level, dead);
            if (running != null) {
                running.onMobDeath(dead, source);
            }
            return;
        }
        if (level.getBlockEntity(BlockPos.of(WildscapesEntityData.of(dead).getLong(ORIGIN))) instanceof CauldronOfSoulsBlockEntity cauldron) {
            cauldron.onMobDeath(dead, source);
        } else if (isEmpowered(dead)) {
            level.sendParticles(WildscapesParticles.LOST_SOUL.get(), dead.getX(), dead.getY(0.6), dead.getZ(),
                    6, 0.3, 0.4, 0.3, 0.02);
        }
    }

    public static boolean keepsDrops(LivingEntity entity) {
        return !WildscapesEntityData.of(entity).contains(ORIGIN);
    }

    public static void onPlayerTick(Player player) {
        if (player.level() instanceof ServerLevel level && Backports.omen(player) != null) {
            CauldronOfSoulsBlockEntity.tryStart(level, player);
        }
    }

    public static void onEntityTick(Entity entity) {
        if (!entity.level().isClientSide || !(entity instanceof Mob mob) || !isEmpowered(entity)) {
            return;
        }
        RandomSource random = mob.getRandom();
        if (random.nextInt(4) != 0) {
            return;
        }
        SimpleParticleType type = switch (random.nextInt(3)) {
            case 0 -> WildscapesParticles.EMPOWERED_WISP.get();
            case 1 -> WildscapesParticles.EMPOWERED_RING.get();
            default -> WildscapesParticles.EMPOWERED_SOUL.get();
        };
        double angle = random.nextDouble() * Math.PI * 2.0;
        double r = entity.getBbWidth() * 0.7 + 0.2;
        entity.level().addParticle(type, entity.getX() + Math.cos(angle) * r,
                entity.getY() + random.nextDouble() * entity.getBbHeight(), entity.getZ() + Math.sin(angle) * r,
                -Math.sin(angle) * 0.03, 0.02, Math.cos(angle) * 0.03);
    }
}
