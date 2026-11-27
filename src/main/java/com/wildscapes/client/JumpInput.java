package com.wildscapes.client;

import net.minecraft.world.item.enchantment.EnchantmentHelper;
import com.wildscapes.entity.JumpEnchantments;
import com.wildscapes.item.WildscapesEnchantments;
import com.wildscapes.network.WildscapesNetwork;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;

public final class JumpInput {
    private static boolean held;
    private static JumpChargeSound sound;

    private JumpInput() {}

    public static void onInput(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        var input = player.input;
        boolean pressed = input.jumping && !held;
        held = input.jumping;
        boolean charging = mc.screen == null && input.shiftKeyDown && JumpEnchantments.canCharge(player);
        if (charging && sound == null) {
            sound = new JumpChargeSound(player);
            mc.getSoundManager().play(sound);
        } else if (!charging && sound != null) {
            mc.getSoundManager().stop(sound);
            sound = null;
        }
        if (mc.screen != null || !JumpEnchantments.canJump(player)) {
            return;
        }
        if (charging) {
            input.jumping = false;
        } else if (pressed && !player.onGround()
                && EnchantmentHelper.getItemEnchantmentLevel(WildscapesEnchantments.MULTI_JUMP.get(), player.getItemBySlot(EquipmentSlot.FEET)) > 0
                && ClientPlayNetworking.canSend(WildscapesNetwork.JUMP)) {
            ClientPlayNetworking.send(WildscapesNetwork.JUMP, PacketByteBufs.empty());
        }
    }
}
