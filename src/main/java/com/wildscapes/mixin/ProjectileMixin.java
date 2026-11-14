package com.wildscapes.mixin;

import com.wildscapes.effect.Intangibility;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Projectile.class)
public abstract class ProjectileMixin {
    @Inject(method = "canHitEntity", at = @At("HEAD"), cancellable = true)
    private void intangibleProjectile(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (target instanceof Player player
                && Intangibility.hiddenFrom(player, ((Projectile) (Object) this).getOwner())) {
            cir.setReturnValue(false);
        }
    }
}
