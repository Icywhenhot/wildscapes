package com.wildscapes.mixin.client;

import com.wildscapes.client.SwampFog;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
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

    @Inject(method = "setupFog", at = @At("RETURN"))
    private void swampFog(Camera camera, int chunks, DeltaTracker delta, float darken,
            ClientLevel level, CallbackInfoReturnable<FogData> cir) {
        if (getFogType(camera) == FogType.NONE) {
            SwampFog.apply(cir.getReturnValue(), level, delta.getGameTimeDeltaPartialTick(false));
        }
    }
}
