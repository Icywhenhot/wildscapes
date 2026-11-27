package com.wildscapes.mixin.client;

import com.wildscapes.client.SwampFog;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
import com.llamalad7.mixinextras.sugar.Local;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Shadow
    private FogType getFogType(Camera camera) {
        throw new AssertionError();
    }

    @Inject(method = "setupFog", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/renderer/fog/FogData;renderDistanceEnd:F", shift = At.Shift.AFTER))
    private void swampFog(Camera camera, int chunks, DeltaTracker delta, float darken,
            ClientLevel level, CallbackInfoReturnable<Vector4f> cir, @Local FogData fog) {
        if (getFogType(camera) == FogType.NONE) {
            SwampFog.apply(fog, level, delta.getGameTimeDeltaPartialTick(false));
        }
    }

    @Inject(method = "computeFogColor", at = @At("RETURN"))
    private void swampColor(Camera camera, float partialTick, ClientLevel level, int chunks, float darken,
            CallbackInfoReturnable<Vector4f> cir) {
        if (getFogType(camera) == FogType.NONE) {
            SwampFog.tint(cir.getReturnValue());
        }
    }
}
