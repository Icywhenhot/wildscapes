package com.wildscapes.network;

import java.util.function.Supplier;

import com.wildscapes.entity.JumpEnchantments;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public record JumpPayload() {
    public static final JumpPayload INSTANCE = new JumpPayload();

    static void handle(JumpPayload packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            JumpEnchantments.jump(player);
        }
    }
}
