package com.wildscapes.effect;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class MirageSync {
    private static final Map<LivingEntity, MobEffectInstance> effects = new WeakHashMap<>();

    private MirageSync() {}

    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)
                || !(entity.level() instanceof ServerLevel level)) {
            return;
        }
        MobEffectInstance effect = entity.getEffect(WildscapesEffects.MIRAGE);
        MobEffectInstance previous = effects.get(entity);
        if (effect == null) {
            if (effects.remove(entity) != null) {
                level.getChunkSource().broadcastAndSend(entity,
                        new ClientboundRemoveMobEffectPacket(entity.getId(), WildscapesEffects.MIRAGE));
            }
            return;
        }
        if (previous == null || effect.getAmplifier() != previous.getAmplifier()
                || effect.isAmbient() != previous.isAmbient() || effect.isVisible() != previous.isVisible()
                || effect.showIcon() != previous.showIcon()
                || effect.getDuration() != (previous.isInfiniteDuration() ? -1 : previous.getDuration() - 1)) {
            level.getChunkSource().broadcastAndSend(entity,
                    new ClientboundUpdateMobEffectPacket(entity.getId(), effect, false));
        }
        effects.put(entity, new MobEffectInstance(effect));
    }

    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof LivingEntity entity) {
            MobEffectInstance effect = entity.getEffect(WildscapesEffects.MIRAGE);
            if (effect != null) {
                player.connection.send(new ClientboundUpdateMobEffectPacket(entity.getId(), effect, false));
            }
        }
    }
}
