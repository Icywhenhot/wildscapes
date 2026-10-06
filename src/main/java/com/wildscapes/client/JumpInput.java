package com.wildscapes.client;

import com.wildscapes.Wildscapes;
import com.wildscapes.entity.JumpEnchantments;
import com.wildscapes.item.MirelashItem;
import com.wildscapes.network.JumpPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Wildscapes.MODID, value = Dist.CLIENT)
public final class JumpInput {
    private static boolean held;
    private static JumpChargeSound sound;

    private JumpInput() {}

    @SubscribeEvent
    static void onInput(MovementInputUpdateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        var player = event.getEntity();
        var input = event.getInput();
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
            input.jumping = false;
            if (pressed) {
                PacketDistributor.sendToServer(new JumpPayload(true));
            }
        } else if (pressed && !player.onGround() && MirelashItem.enchantmentLevel(player.level(),
                player.getItemBySlot(EquipmentSlot.FEET), JumpEnchantments.MULTI_JUMP) > 0) {
            PacketDistributor.sendToServer(new JumpPayload(false));
        }
    }
}
