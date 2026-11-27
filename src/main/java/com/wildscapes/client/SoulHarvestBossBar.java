package com.wildscapes.client;

import com.wildscapes.Wildscapes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

public final class SoulHarvestBossBar {
    private static final ResourceLocation FRAME = Wildscapes.id("textures/gui/soul_harvest.png");
    private static final ResourceLocation FILL = Wildscapes.id("textures/gui/soul_harvest_fill.png");
    private static final String NAME = "event.wildscapes.soul_harvest";

    private SoulHarvestBossBar() {}

    public static boolean matches(BossEvent event) {
        return event.getName().getContents() instanceof TranslatableContents name && name.getKey().startsWith(NAME);
    }

    public static int extract(GuiGraphics gui, BossEvent event, int y) {
        int x = gui.guiWidth() / 2 - 105;
        y -= 9;
        int width = Mth.floor(Mth.clamp(event.getProgress(), 0.0F, 1.0F) * 187);
        RenderSystem.enableBlend();
        gui.blit(FRAME, x, y, 0, 0, 211, 36, 211, 36);
        if (width > 0) {
            gui.blit(FILL, x + 12, y + 17, 0, 0, width, 5, 187, 5);
        }
        RenderSystem.disableBlend();
        if (((TranslatableContents) event.getName().getContents()).getKey().equals(NAME)) {
            return 40;
        }
        gui.drawCenteredString(Minecraft.getInstance().font, event.getName(), gui.guiWidth() / 2, y + 38, -1);
        return 52;
    }
}
