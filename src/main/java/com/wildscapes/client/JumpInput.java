package com.wildscapes.client;

import com.wildscapes.entity.JumpEnchantments;
import com.wildscapes.item.MirelashItem;
import com.wildscapes.network.JumpPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.EquipmentSlot;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class JumpInput {
    private static boolean held;
    private static JumpChargeSound sound;

    private JumpInput() {}

    public static void onInput(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        Input input = player.input.keyPresses;
        boolean pressed = input.jump() && !held;
        held = input.jump();
        boolean charging = mc.gui.screen() == null && input.shift() && JumpEnchantments.canCharge(player);
        if (charging && sound == null) {
            sound = new JumpChargeSound(mc.player);
            mc.getSoundManager().play(sound);
        } else if (!charging && sound != null) {
            mc.getSoundManager().stop(sound);
            sound = null;
        }
        if (mc.gui.screen() != null || !JumpEnchantments.canJump(player)) {
            return;
        }
        if (charging) {
            player.input.keyPresses = new Input(input.forward(), input.backward(), input.left(),
                    input.right(), false, input.shift(), input.sprint());
        } else if (pressed && !player.onGround() && MirelashItem.enchantmentLevel(player.level(),
                player.getItemBySlot(EquipmentSlot.FEET), JumpEnchantments.MULTI_JUMP) > 0) {
            if (ClientPlayNetworking.canSend(JumpPayload.TYPE)) {
                ClientPlayNetworking.send(JumpPayload.INSTANCE);
            }
        }
    }
}
