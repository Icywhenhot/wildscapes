package com.wildscapes.mixin.client;

import com.wildscapes.effect.client.PhaseVision;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void phaseVision(GuiGraphics graphics, float partialTick, CallbackInfo ci) {
        PhaseVision.renderVeil(graphics, partialTick);
    }
}
