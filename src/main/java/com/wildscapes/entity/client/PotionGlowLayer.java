package com.wildscapes.entity.client;

import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.wildscapes.entity.AbominationEntity;

import net.minecraft.resources.Identifier;

public class PotionGlowLayer extends AutoGlowingGeoLayer<AbominationEntity, Void, AbominationRenderer.State> {
    public PotionGlowLayer(AbominationRenderer renderer) {
        super(renderer);
    }

    @Override
    protected Identifier getTextureResource(AbominationRenderer.State state) {
        return AbominationModel.glowFrame(state);
    }
}
