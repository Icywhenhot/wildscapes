package com.wildscapes.mixin.client;

import com.wildscapes.effect.Intangibility;
import com.wildscapes.client.JumpInput;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/ClientInput;tick()V", shift = At.Shift.AFTER))
    private void updateJump(CallbackInfo ci) {
        JumpInput.onInput((LocalPlayer) (Object) this);
    }


    @Inject(method = "moveTowardsClosestSpace(DD)V", at = @At("HEAD"), cancellable = true)
    private void intangibleEscape(double x, double z, CallbackInfo ci) {
        if (Intangibility.isActive((LocalPlayer) (Object) this)) {
            ci.cancel();
        }
    }
}
