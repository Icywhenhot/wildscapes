package com.wildscapes.entity;

import com.mojang.serialization.Codec;
import com.wildscapes.Wildscapes;
import com.wildscapes.block.entity.CauldronOfSoulsBlockEntity;
import com.wildscapes.particle.WildscapesParticles;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
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

    private static final Identifier HEALTH_BOOST =
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "empowered_health");

    public static final AttachmentType<Boolean> EMPOWERED = AttachmentRegistry.<Boolean>builder()
            .persistent(Codec.BOOL)
            .syncWith(ByteBufCodecs.BOOL, AttachmentSyncPredicate.all())
            .buildAndRegister(Wildscapes.id("empowered"));

    public static final AttachmentType<BlockPos> ORIGIN =
            AttachmentRegistry.createPersistent(Wildscapes.id("soul_harvest_origin"), BlockPos.CODEC);

    public static boolean isEmpowered(Entity entity) {
        return entity.getAttachedOrElse(EMPOWERED, false);
    }

    public static void empower(Mob mob) {
        mob.setAttached(EMPOWERED, true);
        AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.addOrReplacePermanentModifier(new AttributeModifier(HEALTH_BOOST, 1.0,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            mob.setHealth(mob.getMaxHealth());
        }
    }

    public static float damage(DamageSource source, float amount) {
        return source.getEntity() instanceof Mob mob && isEmpowered(mob) ? amount * 2.0F : amount;
    }

    public static void onDeath(LivingEntity dead, DamageSource source) {
        if (!(dead.level() instanceof ServerLevel level)) {
            return;
        }
        if (!dead.hasAttached(ORIGIN)) {
            CauldronOfSoulsBlockEntity running = CauldronOfSoulsBlockEntity.harvesting(level, dead);
            if (running != null) {
                running.onMobDeath(dead, source);
            }
            return;
        }
        if (level.getBlockEntity(dead.getAttachedOrThrow(ORIGIN)) instanceof CauldronOfSoulsBlockEntity cauldron) {
            cauldron.onMobDeath(dead, source);
        } else if (isEmpowered(dead)) {
            level.sendParticles(WildscapesParticles.LOST_SOUL, dead.getX(), dead.getY(0.6), dead.getZ(),
                    6, 0.3, 0.4, 0.3, 0.02);
        }
    }

    public static void onPlayerTick(Player player) {
        if (player.level() instanceof ServerLevel level && player.hasEffect(MobEffects.BAD_OMEN)) {
            CauldronOfSoulsBlockEntity.tryStart(level, player);
        }
    }

    public static void onEntityTick(Entity entity) {
        if (!entity.level().isClientSide() || !(entity instanceof Mob) || !isEmpowered(entity)) {
            return;
        }
        RandomSource random = entity.getRandom();
        if (random.nextInt(4) != 0) {
            return;
        }
        SimpleParticleType type = switch (random.nextInt(3)) {
            case 0 -> WildscapesParticles.EMPOWERED_WISP;
            case 1 -> WildscapesParticles.EMPOWERED_RING;
            default -> WildscapesParticles.EMPOWERED_SOUL;
        };
        double angle = random.nextDouble() * Math.PI * 2.0;
        double r = entity.getBbWidth() * 0.7 + 0.2;
        entity.level().addParticle(type, entity.getX() + Math.cos(angle) * r,
                entity.getY() + random.nextDouble() * entity.getBbHeight(), entity.getZ() + Math.sin(angle) * r,
                -Math.sin(angle) * 0.03, 0.02, Math.cos(angle) * 0.03);
    }
}
