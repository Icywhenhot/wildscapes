package com.wildscapes.mixin;

import com.wildscapes.effect.Intangibility;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "hasLineOfSight", at = @At("HEAD"), cancellable = true)
    private void intangibleSight(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player && Intangibility.isActive(player)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = {"isPushable", "canBeSeenByAnyone", "isInWall"}, at = @At("HEAD"), cancellable = true)
    private void intangiblePush(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player && Intangibility.isActive(player)) {
            cir.setReturnValue(false);
        }
    }
}
