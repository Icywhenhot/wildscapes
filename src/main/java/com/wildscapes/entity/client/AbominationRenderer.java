package com.wildscapes.entity.client;

import com.wildscapes.entity.AbominationEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AbominationRenderer extends GeoEntityRenderer<AbominationEntity> {
    public AbominationRenderer(EntityRendererProvider.Context context) {
        super(context, new AbominationModel());
    }
}
