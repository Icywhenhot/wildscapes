package com.wildscapes.entity.client;

import com.wildscapes.Wildscapes;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.state.BoggedRenderState;
import net.minecraft.world.entity.monster.skeleton.Bogged;

public class RedesignedBoggedRenderer extends AbstractSkeletonRenderer<Bogged, BoggedRenderState> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Wildscapes.MODID, "textures/entity/bogged.png");

    public RedesignedBoggedRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.BOGGED_ARMOR,
                new RedesignedBoggedModel(context.bakeLayer(WildscapesModelLayers.BOGGED)));
    }

    @Override
    public BoggedRenderState createRenderState() {
        return new BoggedRenderState();
    }

    @Override
    public void extractRenderState(Bogged bogged, BoggedRenderState state, float partialTick) {
        super.extractRenderState(bogged, state, partialTick);
        state.isSheared = bogged.isSheared();
    }

    @Override
    public Identifier getTextureLocation(BoggedRenderState state) {
        return TEXTURE;
    }
}
