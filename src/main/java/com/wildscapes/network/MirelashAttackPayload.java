package com.wildscapes.network;


import com.wildscapes.item.MirelashItem;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record MirelashAttackPayload(int targetId) {
    void handle(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) {
            return;
        }
        Entity target = player.level().getEntity(targetId);
        if (target instanceof LivingEntity living) {
            MirelashItem.launchAttack(player, living);
        }
    }
}
