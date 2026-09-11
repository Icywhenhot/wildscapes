package com.wildscapes.mixin.client;

import com.wildscapes.effect.WildscapesEffects;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Inject(method = "isBodyVisible", at = @At("HEAD"), cancellable = true)
    private void intangibleBody(T entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity.hasEffect(WildscapesEffects.INTANGIBILITY)) {
            cir.setReturnValue(false);
        }
    }

    @ModifyConstant(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            constant = @Constant(intValue = 654311423))
    private int intangibleOpacity(int color, T entity) {
        return entity.hasEffect(WildscapesEffects.INTANGIBILITY) ? 0x80FFFFFF : color;
    }
}
