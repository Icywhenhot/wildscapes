package com.wildscapes.client;

import com.wildscapes.Wildscapes;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;

@EventBusSubscriber(modid = Wildscapes.MODID, value = Dist.CLIENT)
public final class SoulHarvestBossBar {
    private static final ResourceLocation ICON =
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "textures/mob_effect/soul_harvest.png");

    private SoulHarvestBossBar() {}

    @SubscribeEvent
    static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        if (event.getBossEvent().getName().getContents() instanceof TranslatableContents name
                && name.getKey().startsWith("event.wildscapes.soul_harvest")) {
            event.getGuiGraphics().blit(ICON, event.getX() - 22, event.getY() - 11, 0, 0, 18, 18, 18, 18);
        }
    }
}
