package com.wildscapes.client;

import com.wildscapes.item.MirelashItem;
import com.wildscapes.network.MirelashAttackPayload;

import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class MirelashInput {
    private MirelashInput() {}

    public static boolean onAttack(Minecraft minecraft) {
        if (minecraft.player == null || !canSend(minecraft) || minecraft.gui.screen() != null
                || !(minecraft.player.getMainHandItem().getItem() instanceof MirelashItem)
                || minecraft.player.getCooldowns().isOnCooldown(minecraft.player.getMainHandItem())
                || minecraft.player.getAttackStrengthScale(0.5F) < 0.9F) {
            return false;
        }
        LivingEntity target = MirelashItem.findAttackTarget(minecraft.player.level(), minecraft.player);
        if (target == null) {
            return false;
        }
        ClientPacketDistributor.sendToServer(new MirelashAttackPayload(target.getId()));
        minecraft.player.resetAttackStrengthTicker();
        minecraft.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false);
        return true;
    }

    private static boolean canSend(Minecraft minecraft) {
        ClientPacketListener connection = minecraft.getConnection();
        return connection != null && connection.hasChannel(MirelashAttackPayload.TYPE.id());
    }
}
