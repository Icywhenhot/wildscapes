package com.wildscapes.entity;

import com.mojang.serialization.Codec;
import com.wildscapes.Wildscapes;
import com.wildscapes.block.entity.CauldronOfSoulsBlockEntity;
import com.wildscapes.particle.WildscapesParticles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@EventBusSubscriber(modid = Wildscapes.MODID)
public final class Incursion {
    private Incursion() {}

    private static final ResourceLocation HEALTH_BOOST =
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "empowered_health");

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Wildscapes.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> EMPOWERED =
            ATTACHMENT_TYPES.register("empowered", () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .sync(ByteBufCodecs.BOOL)
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BlockPos>> ORIGIN =
            ATTACHMENT_TYPES.register("incursion_origin", () -> AttachmentType.builder(() -> BlockPos.ZERO)
                    .serialize(BlockPos.CODEC)
                    .build());

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }

    public static boolean isEmpowered(Entity entity) {
        return entity.getData(EMPOWERED);
    }

    public static void empower(Mob mob) {
        mob.setData(EMPOWERED, true);
        AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.addOrReplacePermanentModifier(new AttributeModifier(HEALTH_BOOST, 1.0,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            mob.setHealth(mob.getMaxHealth());
        }
    }

    @SubscribeEvent
    static void onHurt(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof Mob && isEmpowered(attacker)) {
            event.setAmount(event.getAmount() * 2.0F);
        }
    }

    @SubscribeEvent
    static void onDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (!(dead.level() instanceof ServerLevel level) || !dead.hasData(ORIGIN)) {
            return;
        }
        if (level.getBlockEntity(dead.getData(ORIGIN)) instanceof CauldronOfSoulsBlockEntity cauldron) {
            cauldron.onMobDeath(dead, event.getSource());
        } else if (isEmpowered(dead)) {
            level.sendParticles(WildscapesParticles.LOST_SOUL.get(), dead.getX(), dead.getY(0.6), dead.getZ(),
                    6, 0.3, 0.4, 0.3, 0.02);
        }
    }

    @SubscribeEvent
    static void onDrops(LivingDropsEvent event) {
        if (event.getEntity().hasData(ORIGIN)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player.level() instanceof ServerLevel level && player.hasEffect(MobEffects.BAD_OMEN)) {
            CauldronOfSoulsBlockEntity.tryStart(level, player);
        }
    }

    @SubscribeEvent
    static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
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
