package com.wildscapes.entity.client;

import com.wildscapes.Wildscapes;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Bogged;

public class RedesignedBoggedRenderer extends SkeletonRenderer<Bogged> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/bogged.png");

    public RedesignedBoggedRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.BOGGED_INNER_ARMOR, ModelLayers.BOGGED_OUTER_ARMOR,
                new RedesignedBoggedModel(context.bakeLayer(WildscapesModelLayers.BOGGED)));
    }

    @Override
    public ResourceLocation getTextureLocation(Bogged entity) {
        return TEXTURE;
    }
}
