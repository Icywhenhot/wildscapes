package com.wildscapes.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.wildscapes.client.SwampFog;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Shadow private static float fogRed;
    @Shadow private static float fogGreen;
    @Shadow private static float fogBlue;

    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void swampFog(Camera camera, FogRenderer.FogMode mode, float distance, boolean thick,
            float partialTick, CallbackInfo ci) {
        SwampFog.apply(camera, mode, partialTick);
    }

    @Inject(method = "setupColor", at = @At("TAIL"))
    private static void swampColor(Camera camera, float partialTick, ClientLevel level, int chunks,
            float darken, CallbackInfo ci) {
        if (camera.getFluidInCamera() == FogType.NONE) {
            Vector4f color = new Vector4f(fogRed, fogGreen, fogBlue, 0);
            SwampFog.tint(color);
            fogRed = color.x;
            fogGreen = color.y;
            fogBlue = color.z;
            RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0);
        }
    }
}
