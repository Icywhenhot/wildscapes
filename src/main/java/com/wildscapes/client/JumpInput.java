package com.wildscapes.client;

import com.wildscapes.item.WildscapesEnchantments;
import com.wildscapes.Wildscapes;
import com.wildscapes.entity.JumpEnchantments;
import com.wildscapes.network.JumpPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import com.wildscapes.network.WildscapesNetwork;

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
        } else if (pressed && !player.onGround() && player.getItemBySlot(EquipmentSlot.FEET).getEnchantmentLevel(WildscapesEnchantments.MULTI_JUMP.get()) > 0) {
            WildscapesNetwork.CHANNEL.sendToServer(JumpPayload.INSTANCE);
        }
    }
}
