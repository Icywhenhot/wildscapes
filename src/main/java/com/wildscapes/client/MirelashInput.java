package com.wildscapes.client;

import com.wildscapes.Wildscapes;
import com.wildscapes.item.MirelashItem;
import com.wildscapes.network.MirelashAttackPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Wildscapes.MODID, value = Dist.CLIENT)
public final class MirelashInput {
    private MirelashInput() {}

    @SubscribeEvent
    static void onAttack(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!event.isAttack() || minecraft.player == null || minecraft.screen != null
                || !(minecraft.player.getMainHandItem().getItem() instanceof MirelashItem)
                || minecraft.player.getCooldowns().isOnCooldown(minecraft.player.getMainHandItem().getItem())
                || minecraft.player.getAttackStrengthScale(0.5F) < 0.9F) {
            return;
        }
        LivingEntity target = MirelashItem.findAttackTarget(minecraft.player.level(), minecraft.player);
        if (target == null) {
            return;
        }
        PacketDistributor.sendToServer(new MirelashAttackPayload(target.getId()));
        minecraft.player.resetAttackStrengthTicker();
        event.setSwingHand(true);
        event.setCanceled(true);
    }
}
