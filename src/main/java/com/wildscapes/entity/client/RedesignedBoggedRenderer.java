package com.wildscapes.entity.client;

import com.wildscapes.Wildscapes;
import com.wildscapes.entity.Bogged;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class RedesignedBoggedRenderer extends HumanoidMobRenderer<Bogged, RedesignedBoggedModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/bogged.png");

    public RedesignedBoggedRenderer(EntityRendererProvider.Context context) {
        super(context, new RedesignedBoggedModel(context.bakeLayer(WildscapesModelLayers.BOGGED)), 0.5F);
        addLayer(new HumanoidArmorLayer<>(this,
                new SkeletonModel<>(context.bakeLayer(ModelLayers.SKELETON_INNER_ARMOR)),
                new SkeletonModel<>(context.bakeLayer(ModelLayers.SKELETON_OUTER_ARMOR)), context.getModelManager()));
    }

    @Override
    protected boolean isShaking(Bogged entity) {
        return entity.isShaking();
    }

    @Override
    public ResourceLocation getTextureLocation(Bogged entity) {
        return TEXTURE;
    }
}
