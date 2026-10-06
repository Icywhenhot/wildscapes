package com.wildscapes.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.wildscapes.Wildscapes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;

@EventBusSubscriber(modid = Wildscapes.MODID, value = Dist.CLIENT)
public final class SoulHarvestBossBar {
    private static final ResourceLocation FRAME =
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "textures/gui/soul_harvest.png");
    private static final ResourceLocation FILL =
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "textures/gui/soul_harvest_fill.png");

    private SoulHarvestBossBar() {}

    @SubscribeEvent
    static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        if (event.getBossEvent().getName().getContents() instanceof TranslatableContents name
                && name.getKey().startsWith("event.wildscapes.soul_harvest")) {
            event.setCanceled(true);
            GuiGraphics gui = event.getGuiGraphics();
            int x = gui.guiWidth() / 2 - 105;
            int y = event.getY() - 9;
            int width = Mth.floor(Mth.clamp(event.getBossEvent().getProgress(), 0.0F, 1.0F) * 187);
            RenderSystem.enableBlend();
            gui.blit(FRAME, x, y, 0, 0, 211, 36, 211, 36);
            if (width > 0) {
                gui.blit(FILL, x + 12, y + 17, 0, 0, width, 5, 187, 5);
            }
            RenderSystem.disableBlend();
            event.setIncrement(40);
            if (!name.getKey().equals("event.wildscapes.soul_harvest")) {
                gui.drawCenteredString(Minecraft.getInstance().font, event.getBossEvent().getName(),
                        gui.guiWidth() / 2, y + 38, 0xFFFFFF);
                event.setIncrement(52);
            }
        }
    }
}
