package com.wildscapes.mixin;

import java.util.List;

import com.wildscapes.effect.Intangibility;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    protected Vec3 stuckSpeedMultiplier;

    @Inject(method = "move", at = @At("HEAD"))
    private void intangibleStuck(MoverType type, Vec3 movement, CallbackInfo ci) {
        if ((Object) this instanceof Player player && Intangibility.isActive(player)) {
            stuckSpeedMultiplier = Vec3.ZERO;
        }
    }

    @Inject(method = "checkInsideBlocks", at = @At("HEAD"), cancellable = true)
    private void intangibleBlocks(CallbackInfo ci) {
        if ((Object) this instanceof Player player && Intangibility.isActive(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "collide", at = @At("HEAD"), cancellable = true)
    private void intangibleCollision(Vec3 movement, CallbackInfoReturnable<Vec3> cir) {
        Entity entity = (Entity) (Object) this;
        if (!(entity instanceof Player player) || !Intangibility.isActive(player)) {
            return;
        }

        Vec3 fall = Entity.collideBoundingBox(entity, new Vec3(0.0, Math.min(movement.y, 0.0), 0.0),
                entity.getBoundingBox(), entity.level(), List.of());
        cir.setReturnValue(new Vec3(movement.x, movement.y < 0.0 ? fall.y : movement.y, movement.z));
    }
}
