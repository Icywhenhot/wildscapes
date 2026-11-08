package com.wildscapes.mixin.client;

import com.wildscapes.client.MirelashInput;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void mirelashAttack(CallbackInfoReturnable<Boolean> cir) {
        if (MirelashInput.onAttack((Minecraft) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
