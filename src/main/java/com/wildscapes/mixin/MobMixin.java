package com.wildscapes.mixin;

import com.wildscapes.effect.Intangibility;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void intangibleTarget(LivingEntity target, CallbackInfo ci) {
        if (target instanceof Player player && Intangibility.isActive(player)) {
            ((Mob) (Object) this).setTarget(null);
            ci.cancel();
        }
    }
}
