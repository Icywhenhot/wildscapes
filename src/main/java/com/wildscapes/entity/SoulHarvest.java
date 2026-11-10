package com.wildscapes.entity;

import com.wildscapes.network.WildscapesNetwork;

import com.wildscapes.Wildscapes;
import com.wildscapes.block.entity.CauldronOfSoulsBlockEntity;
import com.wildscapes.particle.WildscapesParticles;

import net.minecraft.core.BlockPos;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;

@EventBusSubscriber(modid = Wildscapes.MODID)
public final class SoulHarvest {
    private SoulHarvest() {}

    public static final String EMPOWERED = "wildscapes:empowered";
    public static final String ORIGIN = "wildscapes:soul_harvest_origin";
    private static final java.util.UUID HEALTH_BOOST = java.util.UUID.nameUUIDFromBytes(
            "wildscapes:empowered_health".getBytes(java.nio.charset.StandardCharsets.UTF_8));

    public static boolean isEmpowered(Entity entity) {
        return entity.getPersistentData().getBoolean(EMPOWERED);
    }

    public static void empower(Mob mob) {
        mob.getPersistentData().putBoolean(EMPOWERED, true);
        WildscapesNetwork.sync(mob);
        AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(HEALTH_BOOST);
            health.addPermanentModifier(new AttributeModifier(HEALTH_BOOST, "empowered health", 1.0,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
            mob.setHealth(mob.getMaxHealth());
        }
    }

    @SubscribeEvent
    static void onHurt(LivingHurtEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof Mob && isEmpowered(attacker)) {
            event.setAmount(event.getAmount() * 2.0F);
        }
    }

    @SubscribeEvent
    static void onDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (!(dead.level() instanceof ServerLevel level)) {
            return;
        }
        if (!dead.getPersistentData().contains(ORIGIN)) {
            CauldronOfSoulsBlockEntity running = CauldronOfSoulsBlockEntity.harvesting(level, dead);
            if (running != null) {
                running.onMobDeath(dead, event.getSource());
            }
            return;
        }
        if (level.getBlockEntity(BlockPos.of(dead.getPersistentData().getLong(ORIGIN))) instanceof CauldronOfSoulsBlockEntity cauldron) {
            cauldron.onMobDeath(dead, event.getSource());
        } else if (isEmpowered(dead)) {
            level.sendParticles(WildscapesParticles.LOST_SOUL.get(), dead.getX(), dead.getY(0.6), dead.getZ(),
                    6, 0.3, 0.4, 0.3, 0.02);
        }
    }

    @SubscribeEvent
    static void onDrops(LivingDropsEvent event) {
        if (event.getEntity().getPersistentData().contains(ORIGIN)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        Player player = event.player;
        if (player.level() instanceof ServerLevel level && player.hasEffect(MobEffects.BAD_OMEN)) {
            CauldronOfSoulsBlockEntity.tryStart(level, player);
        }
    }

    public static void onEntityTick(Entity entity) {
        if (!entity.level().isClientSide || !(entity instanceof Mob) || !isEmpowered(entity)) {
            return;
        }
        RandomSource random = entity.getRandom();
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
