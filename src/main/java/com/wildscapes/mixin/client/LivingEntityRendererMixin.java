package com.wildscapes.mixin.client;

import com.wildscapes.effect.WildscapesEffects;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), cancellable = true)
    private void intangiblePlayer(T entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffer,
            int light, CallbackInfo ci) {
        if (entity.hasEffect(WildscapesEffects.INTANGIBILITY) && Minecraft.getInstance().player != entity) {
            ci.cancel();
        }
    }

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
