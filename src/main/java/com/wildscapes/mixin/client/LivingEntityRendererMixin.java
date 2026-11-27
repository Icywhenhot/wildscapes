package com.wildscapes.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildscapes.effect.WildscapesEffects;
import com.wildscapes.entity.SoulHarvest;
import com.wildscapes.entity.SwampVariants;
import com.wildscapes.entity.client.MirageLayer;
import com.wildscapes.entity.client.MobVisuals;
import com.wildscapes.entity.client.SlimeBubbleLayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Shadow
    protected abstract boolean addLayer(RenderLayer<S, M> layer);

    @Inject(method = "<init>", at = @At("TAIL"))
    @SuppressWarnings("unchecked")
    private void addWildscapesLayers(EntityRendererProvider.Context context, M model, float shadow, CallbackInfo ci) {
        var renderer = (LivingEntityRenderer<T, S, M>) (Object) this;
        addLayer(new MirageLayer<>(renderer));
        addLayer(new SlimeBubbleLayer<>(renderer, context.getModelSet()));
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void extractWildscapesState(T entity, S state, float partialTick, CallbackInfo ci) {
        state.setRenderData(MobVisuals.EMPOWERED, SoulHarvest.isEmpowered(entity));
        state.setRenderData(MobVisuals.SWAMP, SwampVariants.isSwampBorn(entity));
        state.setRenderData(MobVisuals.MIRAGE, entity.hasEffect(WildscapesEffects.MIRAGE));
        state.setRenderData(MobVisuals.BUBBLE, entity.hasEffect(WildscapesEffects.SLIMEBOUND));
        state.setRenderData(MobVisuals.PHASED, entity.hasEffect(WildscapesEffects.INTANGIBILITY));
        state.setRenderData(MobVisuals.LOCAL, Minecraft.getInstance().player == entity);
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void intangiblePlayer(S state, PoseStack pose, SubmitNodeCollector nodes, CameraRenderState camera, CallbackInfo ci) {
        if (state.getRenderDataOrDefault(MobVisuals.PHASED, false) && !state.getRenderDataOrDefault(MobVisuals.LOCAL, false)) {
            ci.cancel();
        }
    }

    @Inject(method = "isBodyVisible", at = @At("HEAD"), cancellable = true)
    private void intangibleBody(S state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getRenderDataOrDefault(MobVisuals.PHASED, false)) {
            cir.setReturnValue(false);
        }
    }

    @ModifyConstant(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            constant = @Constant(intValue = 654311423))
    private int intangibleOpacity(int color, S state) {
        return state.getRenderDataOrDefault(MobVisuals.PHASED, false) ? 0x80FFFFFF : color;
    }
}
