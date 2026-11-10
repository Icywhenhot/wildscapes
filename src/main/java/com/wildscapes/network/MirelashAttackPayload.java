package com.wildscapes.network;

import java.util.function.Supplier;

import com.wildscapes.item.MirelashItem;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

public record MirelashAttackPayload(int targetId) {
    static void handle(MirelashAttackPayload packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player == null || !player.isAlive() || player.isSpectator()) {
            return;
        }
        Entity target = player.level().getEntity(packet.targetId());
        if (target instanceof LivingEntity living) {
            MirelashItem.launchAttack(player, living);
        }
    }
}
