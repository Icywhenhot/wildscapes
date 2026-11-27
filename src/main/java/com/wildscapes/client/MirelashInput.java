package com.wildscapes.client;

import com.wildscapes.item.MirelashItem;
import com.wildscapes.network.WildscapesNetwork;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public final class MirelashInput {
    private MirelashInput() {}

    public static boolean onAttack(Minecraft minecraft) {
        if (!ClientPlayNetworking.canSend(WildscapesNetwork.MIRELASH_ATTACK) || minecraft.player == null
                || minecraft.screen != null
                || !(minecraft.player.getMainHandItem().getItem() instanceof MirelashItem)
                || minecraft.player.getCooldowns().isOnCooldown(minecraft.player.getMainHandItem().getItem())
                || minecraft.player.getAttackStrengthScale(0.5F) < 0.9F) {
            return false;
        }
        LivingEntity target = MirelashItem.findAttackTarget(minecraft.player.level(), minecraft.player);
        if (target == null) {
            return false;
        }
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(target.getId());
        ClientPlayNetworking.send(WildscapesNetwork.MIRELASH_ATTACK, buf);
        minecraft.player.resetAttackStrengthTicker();
        minecraft.player.swing(InteractionHand.MAIN_HAND);
        return true;
    }
}
