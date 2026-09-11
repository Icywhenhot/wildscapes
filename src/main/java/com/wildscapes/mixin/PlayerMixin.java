package com.wildscapes.mixin;

import com.wildscapes.effect.Intangibility;

import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "maybeBackOffFromEdge", at = @At("HEAD"), cancellable = true)
    private void intangibleCrouchMove(Vec3 movement, MoverType mover,
            CallbackInfoReturnable<Vec3> cir) {
        Player player = (Player) (Object) this;
        if (Intangibility.isActive(player)) {
            cir.setReturnValue(movement);
        }
    }

    @Inject(method = "canBeHitByProjectile", at = @At("HEAD"), cancellable = true)
    private void intangibleProjectile(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (Intangibility.isActive(player)) {
            cir.setReturnValue(false);
        }
    }
}
