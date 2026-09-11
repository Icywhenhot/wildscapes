package com.wildscapes.mixin;

import com.wildscapes.effect.Intangibility;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Warden.class)
public abstract class WardenMixin {
    @Inject(method = "canTargetEntity", at = @At("HEAD"), cancellable = true)
    private void intangibleTarget(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player && Intangibility.isActive(player)) {
            cir.setReturnValue(false);
        }
    }
}
