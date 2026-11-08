package com.wildscapes.client;

import com.wildscapes.item.MirelashItem;
import com.wildscapes.network.MirelashAttackPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class MirelashInput {
    private MirelashInput() {}

    public static boolean onAttack(Minecraft minecraft) {
        if (!ClientPlayNetworking.canSend(MirelashAttackPayload.TYPE) || minecraft.player == null || minecraft.gui.screen() != null
                || !(minecraft.player.getMainHandItem().getItem() instanceof MirelashItem)
                || minecraft.player.getCooldowns().isOnCooldown(minecraft.player.getMainHandItem())
                || minecraft.player.getAttackStrengthScale(0.5F) < 0.9F) {
            return false;
        }
        LivingEntity target = MirelashItem.findAttackTarget(minecraft.player.level(), minecraft.player);
        if (target == null) {
            return false;
        }
        ClientPlayNetworking.send(new MirelashAttackPayload(target.getId()));
        minecraft.player.resetAttackStrengthTicker();
        minecraft.player.swing(InteractionHand.MAIN_HAND);
        return true;
    }
}
