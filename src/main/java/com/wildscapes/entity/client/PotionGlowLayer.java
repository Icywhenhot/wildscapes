package com.wildscapes.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wildscapes.entity.AbominationEntity;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class PotionGlowLayer extends GeoRenderLayer<AbominationEntity> {
    public PotionGlowLayer(GeoRenderer<AbominationEntity> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack pose, AbominationEntity animatable, BakedGeoModel model, RenderType type,
            MultiBufferSource buffer, VertexConsumer consumer, float partialTick, int light, int overlay) {
        RenderType glow = RenderType.entityTranslucentEmissive(AbominationModel.glowFrame(animatable));
        getRenderer().reRender(model, pose, buffer, animatable, glow, buffer.getBuffer(glow), partialTick,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
    }
}
