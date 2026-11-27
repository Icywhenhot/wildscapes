package com.wildscapes.client;

import com.wildscapes.entity.JumpEnchantments;
import com.wildscapes.item.MirelashItem;
import com.wildscapes.network.JumpPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.world.entity.EquipmentSlot;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class JumpInput {
    private static boolean held;
    private static JumpChargeSound sound;

    private JumpInput() {}

    public static void onInput(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        Input input = player.input;
        boolean pressed = input.jumping && !held;
        held = input.jumping;
        boolean charging = mc.screen == null && input.shiftKeyDown && JumpEnchantments.canCharge(player);
        if (charging && sound == null) {
            sound = new JumpChargeSound(mc.player);
            mc.getSoundManager().play(sound);
        } else if (!charging && sound != null) {
            mc.getSoundManager().stop(sound);
            sound = null;
        }
        if (mc.screen != null || !JumpEnchantments.canJump(player)) {
            return;
        }
        if (charging) {
            player.input.jumping = false;
        } else if (pressed && !player.onGround() && MirelashItem.enchantmentLevel(player.level(),
                player.getItemBySlot(EquipmentSlot.FEET), JumpEnchantments.MULTI_JUMP) > 0) {
            if (ClientPlayNetworking.canSend(JumpPayload.TYPE)) {
                ClientPlayNetworking.send(JumpPayload.INSTANCE);
            }
        }
    }
}
