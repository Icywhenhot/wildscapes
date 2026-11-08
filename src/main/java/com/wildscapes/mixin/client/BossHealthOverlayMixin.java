package com.wildscapes.mixin.client;

import java.util.Map;
import java.util.UUID;

import com.wildscapes.client.SoulHarvestBossBar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayMixin {
    @Shadow @Final
    private Map<UUID, LerpingBossEvent> events;

    @Shadow
    private void extractBar(GuiGraphicsExtractor gui, int x, int y, BossEvent event) {
        throw new AssertionError();
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void extractSoulHarvest(GuiGraphicsExtractor gui, CallbackInfo ci) {
        if (events.values().stream().noneMatch(SoulHarvestBossBar::matches)) {
            return;
        }
        ci.cancel();
        gui.nextStratum();
        int center = gui.guiWidth() / 2;
        int y = 12;
        for (LerpingBossEvent event : events.values()) {
            if (SoulHarvestBossBar.matches(event)) {
                y += SoulHarvestBossBar.extract(gui, event, y);
            } else {
                extractBar(gui, center - 91, y, event);
                gui.centeredText(Minecraft.getInstance().font, event.getName(), center, y - 9, -1);
                y += 19;
            }
            if (y >= gui.guiHeight() / 3) {
                break;
            }
        }
    }
}
