package com.wildscapes.mixin.client;

import com.wildscapes.effect.Intangibility;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelExtractor.class)
public abstract class ScreenEffectRendererMixin {
    @Inject(method = "getViewBlockingState", at = @At("HEAD"), cancellable = true)
    private static void intangibleView(LocalPlayer player, Frustum frustum, CallbackInfoReturnable<BlockState> cir) {
        if (Intangibility.isActive(player)) {
            cir.setReturnValue(null);
        }
    }
}
