package com.wildscapes.block;

import com.wildscapes.Wildscapes;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;

public final class PavShrooms {
    private PavShrooms() {}

    public static void onTick(Player ticking) {
        if (!(ticking instanceof ServerPlayer player) || player.tickCount % 50 != 0
                || !player.isAlive() || player.isSpectator() || player.getHealth() >= player.getMaxHealth()
                || player.hasEffect(MobEffects.REGENERATION)) {
            return;
        }
        for (BlockPos pos : BlockPos.betweenClosed(player.blockPosition().offset(-3, -3, -3),
                player.blockPosition().offset(3, 3, 3))) {
            if (pos.distToCenterSqr(player.position()) > 9.0 || !player.level().hasChunkAt(pos)) continue;
            var state = player.level().getBlockState(pos);
            if (state.is(WildscapesBlocks.LUMINOUS_MUSHROOM) || state.is(WildscapesBlocks.SHELF_LUMINOUS_MUSHROOM)) {
                player.heal(0.5F);
                return;
            }
        }
    }
}
