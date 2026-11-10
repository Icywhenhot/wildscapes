package com.wildscapes.mixin;

import com.wildscapes.entity.SlimeMerging;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slime.class)
public abstract class SlimeMixin {
    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void mergeSize(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        cir.setReturnValue(cir.getReturnValue().scale(SlimeMerging.scale((Slime) (Object) this)));
    }
}
