package com.wildscapes.network;


import com.wildscapes.entity.JumpEnchantments;

import net.minecraft.server.level.ServerPlayer;

public final class JumpPayload {
    private JumpPayload() {}

    static void handle(ServerPlayer player) {
        JumpEnchantments.jump(player);
    }
}
